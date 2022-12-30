/*
 * Copyright (c) 2022-2022. PengYunNetWork
 *
 * This program is free software: you can use, redistribute, and/or modify it
 * under the terms of the GNU Affero General Public License, version 3 or later ("AGPL"),
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  You should have received a copy of the GNU Affero General Public License along with
 *  this program. If not, see <http://www.gnu.org/licenses/>.
 */

package py.storage.async;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.DirectAlignedBufferAllocator;
import py.exception.StorageException;
import py.storage.impl.AsyncStorage;
import py.storage.impl.AsynchronousFileChannelStorageFactory;

public class AsyncIoStat {
  private static final Logger logger = LoggerFactory.getLogger(AsyncIoStat.class);
  AsynchronousFileChannelStorageFactory asynchronousFileChannelStorageFactory;
  private AsyncStorage asyncStorage = null;
  private String devName;

  public boolean randomReadChecker(int blockSize, int iopsThreshold) {
    IoRead ioRead = new IoRead();
    boolean isSlowDisk = false;
    try {
      isSlowDisk = slowDiskChecker(ioRead, devName, blockSize, iopsThreshold, true);
    } catch (StorageException e) {
      logger.warn("random read exception.", e);
    }
    return isSlowDisk;
  }

  public boolean randomWriteChecker(int blockSize, int iopsThreshold) {
    IoWrite ioWrite = new IoWrite();
    boolean isSlowDisk = false;
    try {
      isSlowDisk = slowDiskChecker(ioWrite, devName, blockSize, iopsThreshold, true);
    } catch (StorageException e) {
      logger.warn("random write exception.", e);
    }
    return isSlowDisk;
  }

  public boolean sequentialReadChecker(int blockSize, int ipsThreshold) {
    IoRead ioRead = new IoRead();
    boolean isSlowDisk = false;
    try {
      isSlowDisk = slowDiskChecker(ioRead, devName, blockSize, ipsThreshold, false);
    } catch (StorageException e) {
      logger.warn("sequential read exception.", e);
    }
    return isSlowDisk;
  }

  public boolean sequentialWriteChecker(int blockSize, int iopsThreshold) {
    IoWrite ioWrite = new IoWrite();
    boolean isSlowDisk = false;
    try {
      isSlowDisk = slowDiskChecker(ioWrite, devName, blockSize, iopsThreshold, false);
    } catch (StorageException e) {
      logger.warn("sequential write exception.", e);
    }
    return isSlowDisk;
  }

  public void open(String devName) throws StorageException {
    this.devName = devName;
    try {
      asynchronousFileChannelStorageFactory = AsynchronousFileChannelStorageFactory.getInstance()
          .setMaxThreadpoolSizePerStorage(2).setMaxThreadpoolSizePerSsd(2);
      asyncStorage = (AsyncStorage) asynchronousFileChannelStorageFactory.generate(devName);
      asyncStorage.open();
    } catch (StorageException e) {
      logger.error("open {} error.", devName);
      throw e;
    }
  }

  public void close() throws StorageException {
    if (asyncStorage != null) {
      asyncStorage.close();
    }

    if (asynchronousFileChannelStorageFactory != null) {
      asynchronousFileChannelStorageFactory.close();
    }
  }

  public boolean slowDiskChecker(IoStat ioStat, String deviceName, int blockSize, int iopsThreshold,
      boolean isRandom)
      throws StorageException {
    boolean isSlowDisk = false;
    int ioDepth = 32;
    int ailgn = 512;
    int ioCount = iopsThreshold * 3;
    long offset = 0;
    long startTime = 0;
    long endTime = 0;
    Path path = Paths.get(deviceName);

    RandomDataGenerator random = new RandomDataGenerator();
    long maxRange = asyncStorage.size() / blockSize;

    CountDownLatch countDownLatchSequential = new CountDownLatch(ioCount);
    startTime = System.currentTimeMillis();
    for (int i = 0; i < ioCount; i++) {
      if (offset >= asyncStorage.size() - blockSize) {
        offset = 0;
      }

      ByteBuffer byteBuffer = DirectAlignedBufferAllocator
          .allocateAlignedByteBuffer(blockSize, ailgn);
      ioStat.doIo(asyncStorage, byteBuffer, offset, countDownLatchSequential,
          new CompletionHandler<Integer, CountDownLatch>() {
            @Override
            public void completed(Integer result, CountDownLatch attachment) {
              attachment.countDown();
            }

            @Override
            public void failed(Throwable exc, CountDownLatch attachment) {
              attachment.countDown();
              logger.warn("{} sequential read failed.", deviceName);
            }
          });

      if (isRandom) {
        offset = (random.nextLong(0, maxRange - 1)) * blockSize;
      } else {
        offset = offset + blockSize;
      }
    }

    try {
      countDownLatchSequential.await();
      endTime = System.currentTimeMillis();

    } catch (InterruptedException e) {
      logger.error("storage:{} IOPS check exception. ", deviceName, e);
    }
    if (endTime <= startTime) {
      return false;
    }

    long cost = endTime - startTime;
    long iops = ioCount * 1000 / cost;

    logger
        .info("device:{} IOPS:{} block size:{} threshold:{} random:{} iostat:{}", deviceName, iops,
            blockSize,
            iopsThreshold, isRandom, ioStat);
    return iops > iopsThreshold ? false : true;
  }

  interface IoStat {
    <A> void doIo(AsyncStorage asyncFileStorage, ByteBuffer buffer, long pos, A attachment,
        CompletionHandler<Integer, ? super A> handler) throws StorageException;
  }

  class IoRead implements IoStat {
    @Override
    public <A> void doIo(AsyncStorage asyncStorage, ByteBuffer buffer, long offset, A attachment,
        CompletionHandler<Integer, ? super A> handler) throws StorageException {
      asyncStorage.read(buffer, offset, attachment, handler);
    }

    @Override
    public String toString() {
      return "IORead{read}";
    }
  }

  class IoWrite implements IoStat {
    @Override
    public <A> void doIo(AsyncStorage asyncStorage, ByteBuffer buffer, long offset, A attachment,
        CompletionHandler<Integer, ? super A> handler) throws StorageException {
      asyncStorage.write(buffer, offset, attachment, handler);
    }

    @Override
    public String toString() {
      return "IOWrite{write}";
    }
  }
}
