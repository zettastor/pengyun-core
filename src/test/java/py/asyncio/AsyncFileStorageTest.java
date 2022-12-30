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

package py.asyncio;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.DirectAlignedBufferAllocator;
import py.common.Utils;
import py.common.tlsf.bytebuffer.manager.TlsfByteBufferManagerFactory;
import py.exception.StorageException;
import py.storage.Storage;
import py.storage.async.AsyncFileStorage;
import py.storage.async.Callback;
import py.storage.impl.AsyncStorage;
import py.storage.impl.AsynchronousFileChannelStorageFactory;
import py.test.TestBase;

public class AsyncFileStorageTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(AsyncFileStorageTest.class);
  String fileName = "/tmp/async-io.data";
  private Path path = Paths.get(fileName);

  @Before
  public void addSystemProperty() {
    addSystemProperty("java.library.path",
        System.getProperty("user.dir") + "/src/main/resources/liblinux-async-io");
  }

  private void addSystemProperty(String key, String value) {
    try {
      Field field = ClassLoader.class.getDeclaredField("usr_paths");
      field.setAccessible(true);
      String[] properties = (String[]) field.get(null);
      for (String str : properties) {
        if (value.equals(str)) {
          return;
        }
      }
      String[] propertiesDest = new String[properties.length + 1];
      System.arraycopy(properties, 0, propertiesDest, 0, properties.length);
      propertiesDest[properties.length] = value;
      field.set(null, propertiesDest);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  private AtomicLong testWrite(AsyncStorage asyncFileStorage, ByteBuffer byteBuffer, long count,
      int blockSize)
      throws Exception {
    AtomicLong recviveCount = new AtomicLong(0L);
    for (int i = 0; i < count; i++) {
      asyncFileStorage
          .write(byteBuffer, i * blockSize, recviveCount,
              new CompletionHandler<Integer, AtomicLong>() {
                @Override
                public void completed(Integer result, AtomicLong attachment) {
                  logger.warn("result:{}", result);
                  attachment.addAndGet(1);
                }

                @Override
                public void failed(Throwable exc, AtomicLong attachment) {
                  Validate.isTrue(false);
                }
              });
    }

    watiIoFinished(recviveCount, count);
    return recviveCount;
  }

  @Before
  public void initEnv() throws IOException {
    Files.deleteIfExists(path);
    try {
      if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
        if (!Files.exists(path.getParent())) {
          Files.createDirectories(path.getParent());
        }
        Files.createFile(path);
      }
    } catch (Exception e) {
      logger.error("create file caught an exception ", e);
    }

    byte[] data = new byte[16 * 1024 * 1024];
    FileUtils.writeByteArrayToFile(path.toFile(), data);
    TlsfByteBufferManagerFactory.init(512, 1024 * 1024, true);
  }

  @After
  public void after() throws IOException {
    Files.deleteIfExists(path);
  }

  public void watiIoFinished(AtomicLong request, long result) throws Exception {
    Utils.waitUntilConditionMatches(5, () -> request.get() == result);
  }

  public AsyncStorage storageOpen(int iodepth, int threadCount, Path path) throws Exception {
    AsynchronousFileChannelStorageFactory asyncStorageFactory
        = AsynchronousFileChannelStorageFactory.getInstance();
    asyncStorageFactory.setMaxThreadpoolSizePerStorage(threadCount);
    asyncStorageFactory.setMaxThreadpoolSizePerSsd(threadCount);
    asyncStorageFactory.setMaxIoDepthHdd(iodepth);
    asyncStorageFactory.setMaxIoDepthSsd(iodepth);

    Storage storage = asyncStorageFactory.generate(path.toString());
    assertNotNull(storage);

    assertTrue(storage instanceof AsyncStorage);
    @SuppressWarnings("unchecked")
    AsyncStorage asyncStorage = (AsyncStorage) storage;

    return asyncStorage;
  }

  @Test
  public void testAsyncIoWrite() throws Exception {
    int length = 4096;
    int ailgn = 512;
    ByteBuffer byteBuffer = DirectAlignedBufferAllocator
        .allocateAlignedByteBuffer((int) length, ailgn);
    int ioDepth = 32;
    long count = 256;

    AsyncStorage asyncFileStorage = storageOpen(ioDepth, 1, path);
    asyncFileStorage.open();
    AtomicLong returnCount = testWrite(asyncFileStorage, byteBuffer, count, length);
    watiIoFinished(returnCount, count);
    asyncFileStorage.close();
    Validate.isTrue(returnCount.get() == count);
  }

  @Test
  public void testAsyncIoRead() throws Exception {
    int length = 512;
    int ailgn = 512;
    int ioDepth = 32;
    int count = 256;

    AsyncStorage asyncFileStorage = storageOpen(ioDepth, 1, path);
    asyncFileStorage.open();
    AtomicLong readCount = new AtomicLong(0);
    for (int i = 0; i < count; i++) {
      ByteBuffer byteBuffer = DirectAlignedBufferAllocator
          .allocateAlignedByteBuffer((int) length, ailgn);
      asyncFileStorage.read(byteBuffer, 0, readCount, new CompletionHandler<Integer, AtomicLong>() {
        @Override
        public void completed(Integer result, AtomicLong attachment) {
          if (result > 0) {
            attachment.addAndGet(1);
          }
          logger.warn("result:{}", result);
        }

        @Override
        public void failed(Throwable exc, AtomicLong attachment) {
          Validate.isTrue(false);
        }
      });

    }

    watiIoFinished(readCount, count);
    asyncFileStorage.close();
    Validate.isTrue(readCount.get() == count);
  }

  @Test
  public void testMultThreadWrite() throws Exception {
    int length = 512;
    int ailgn = 512;
    ByteBuffer byteBuffer = DirectAlignedBufferAllocator
        .allocateAlignedByteBuffer((int) length, ailgn);
    int ioDepth = 32;

    AsyncStorage asyncFileStorage = storageOpen(ioDepth, 1, path);
    asyncFileStorage.open();

    for (int i = 0; i < 10; i++) {
      new Thread("Mult-Thread-Write" + i) {
        public void run() {
          try {
            testWrite(asyncFileStorage, byteBuffer, 10, length);
          } catch (Exception e) {
            Assert.fail();
          }
        }
      }.start();
    }
    Thread.sleep(3000);
    asyncFileStorage.close();
  }

  private void testNoBlockWrite(AsyncStorage asyncFileStorage, ByteBuffer byteBuffer, long count,
      int blockSize,
      AtomicLong completeCounter, AtomicLong failedCounter) throws Exception {
    for (int i = 0; i < count; i++) {
      asyncFileStorage
          .write(byteBuffer, i * blockSize, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
              completeCounter.incrementAndGet();
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
              failedCounter.incrementAndGet();
            }
          });
    }
  }

  @Test
  public void testOfflineDisk() throws Exception {
    int length = 8704;
    int ailgn = 512;
    int threadCount = 5;
    int wrinterCount = 10000;
    ByteBuffer byteBuffer = DirectAlignedBufferAllocator
        .allocateAlignedByteBuffer((int) length, ailgn);
    int ioDepth = 32;
    AsyncFileStorage asyncFileStorage = new AsyncFileStorage(path, ioDepth, 512);

    asyncFileStorage.open();
    AtomicLong completeCounter = new AtomicLong(0L);
    AtomicLong failedCounter = new AtomicLong(0L);

    for (int i = 0; i < threadCount; i++) {
      new Thread("Mult-Thread-Write" + i) {
        public void run() {
          try {
            testNoBlockWrite(asyncFileStorage, byteBuffer, wrinterCount, length, completeCounter,
                failedCounter);
          } catch (Exception e) {
            Assert.fail();
          }
        }
      }.start();
    }
    Thread.sleep(500);

    asyncFileStorage.close();

    logger.warn("before complete:{} failed:{}", completeCounter.get(), failedCounter.get());
    int sleepTime = 50;
    while (sleepTime > 0) {
      if (completeCounter.get() + failedCounter.get() == wrinterCount * threadCount) {
        logger.warn("finished");
        break;
      }
      logger.warn("complete:{} failed:{}", completeCounter.get(), failedCounter.get());
      Thread.sleep(1000);
      sleepTime--;
    }
    logger.warn("finished complete:{} failed:{}", completeCounter.get(), failedCounter.get());
    Validate.isTrue(sleepTime > 0);
  }

  @Test
  public void testCheckData() throws Exception {
    int length = 1024;
    int ailgn = 512;

    AsyncStorage asyncStorage = storageOpen(32, 1, path);
    asyncStorage.open();
    ByteBuffer byteBuffer1 = DirectAlignedBufferAllocator
        .allocateAlignedByteBuffer((int) length, ailgn);
    for (int i = 0; i < byteBuffer1.capacity(); i++) {
      byteBuffer1.put((byte) '2');
    }

    final CountDownLatch writeLatch = new CountDownLatch(1);
    CompletionHandler<Integer, Object> handler1 = new CompletionHandler<Integer, Object>() {
      @Override
      public void completed(Integer result, Object attachment) {
        writeLatch.countDown();
        logger.info("write completed");
      }

      @Override
      public void failed(Throwable exc, Object attachment) {
        writeLatch.countDown();
      }
    };

    byteBuffer1.clear();
    asyncStorage.write(byteBuffer1, 0, byteBuffer1, handler1);
    writeLatch.await();

    final CountDownLatch readLatch = new CountDownLatch(1);
    CompletionHandler<Integer, Object> handler2 = new CompletionHandler<Integer, Object>() {
      @Override
      public void completed(Integer result, Object attachment) {
        readLatch.countDown();
        logger.info("read completed");
      }

      @Override
      public void failed(Throwable exc, Object attachment) {
        logger.error("read failed");
        readLatch.countDown();
      }
    };

    ByteBuffer byteBuffer2 = DirectAlignedBufferAllocator
        .allocateAlignedByteBuffer((int) length, ailgn);
    asyncStorage.read(byteBuffer2, 0, byteBuffer2, handler2);
    readLatch.await();

    for (int i = 0; i < byteBuffer2.capacity(); i++) {
      assertTrue(byteBuffer2.get(i) == byteBuffer1.get(i));
    }

    asyncStorage.close();
  }

  private void diplayByteBuffer(ByteBuffer byteBuffer, int start, int end) {
    StringBuilder sb = new StringBuilder();
    int position = byteBuffer.position();
    byteBuffer.position(start);
    for (int i = start; i < end; i++) {
      sb.append(String.format("[%d]:%02X ", i, byteBuffer.get()));
    }
    byteBuffer.position(position);
    logger.warn("byteBuffer:{}", sb);
  }

  @Test
  public void testCheckDataByDiffPosition() throws Exception {
    int pagePhysiclSize = 8704;
    int pageSize = 8192;

    AsyncStorage asyncStorage = storageOpen(32, 1, path);
    asyncStorage.open();
    ByteBuffer byteBuffer1 = ByteBuffer.allocate(pagePhysiclSize);
    byteBuffer1.clear();
    int offset = 10240;
    generateDataByOffset(byteBuffer1, pagePhysiclSize, offset);
    diplayByteBuffer(byteBuffer1, 0, pagePhysiclSize);
    byteBuffer1.clear();
    int pageMetdataLen = 512;
    byteBuffer1.position(pageMetdataLen);
    boolean success = checkDataByOffset(byteBuffer1, offset + pageMetdataLen);
    Validate.isTrue(success);
    final CountDownLatch writeLatch = new CountDownLatch(1);
    CompletionHandler<Integer, Object> handler1 = new CompletionHandler<Integer, Object>() {
      @Override
      public void completed(Integer result, Object attachment) {
        writeLatch.countDown();
        logger.info("write completed");
      }

      @Override
      public void failed(Throwable exc, Object attachment) {
        writeLatch.countDown();
      }
    };

    byteBuffer1.clear();
    asyncStorage.write(byteBuffer1, offset, byteBuffer1, handler1);
    writeLatch.await();

    final CountDownLatch readLatch = new CountDownLatch(1);
    CompletionHandler<Integer, Object> handler2 = new CompletionHandler<Integer, Object>() {
      @Override
      public void completed(Integer result, Object attachment) {
        readLatch.countDown();
        logger.info("read completed");
      }

      @Override
      public void failed(Throwable exc, Object attachment) {
        logger.error("read failed");
        readLatch.countDown();
      }
    };

    int ailgn = 512;

    ByteBuffer byteBuffer2 = DirectAlignedBufferAllocator
        .allocateAlignedByteBuffer((int) pagePhysiclSize, ailgn);

    byteBuffer2.position(pageMetdataLen);
    asyncStorage.read(byteBuffer2, offset + pageMetdataLen, byteBuffer2, handler2);
    readLatch.await();

    diplayByteBuffer(byteBuffer2, pageMetdataLen, pagePhysiclSize);

    byteBuffer2.position(pageMetdataLen);
    success = checkDataByOffset(byteBuffer2, offset + pageMetdataLen);
    Validate.isTrue(success);

    asyncStorage.close();
  }

  @Test
  public void testOpenFailed() throws Exception {
    Path failPath = Paths.get("/xxxxxx/a.log");

    int length = 1024;
    int ailgn = 512;

    boolean expFlag = false;
    try {
      AsyncStorage asyncStorage = storageOpen(32, 1, failPath);
      asyncStorage.open();
    } catch (StorageException e) {
      expFlag = true;
    }

    Validate.isTrue(expFlag);
  }

  @Test
  public void testDirectByteBuffer() throws Exception {
    int length = 512;
    int ailgn = 512;
    int ioDepth = 32;

    AsyncStorage asyncFileStorage = storageOpen(ioDepth, 1, path);
    asyncFileStorage.open();
    for (int i = 0; i < 256; i++) {
      ByteBuffer byteBuffer = ByteBuffer.allocate(length);
      asyncFileStorage.read(byteBuffer, 0, null, new CompletionHandler<Integer, Void>() {
        @Override
        public void completed(Integer result, Void attachment) {
          logger.warn("result:{}", result);
        }

        @Override
        public void failed(Throwable exc, Void attachment) {
          Validate.isTrue(false);
        }
      });
    }

    asyncFileStorage.close();
  }

  @Test
  public void testSlowDiskSequentialWrite() throws Exception {
    int length = 512;
    int ailgn = 512;
    int ioDepth = 32;
    int count = 100;

    CountDownLatch countDownLatch = new CountDownLatch(count);
    AsyncFileStorage asyncFileStorage = new AsyncFileStorage(path, ioDepth, 512);
    asyncFileStorage.open();
    asyncFileStorage.enableSlowDisk(100, 1000, 0.5, 1, new Callback() {
      @Override
      public void done(int errCode) {
        logger.warn("find slow disk.{}", asyncFileStorage.toString());
      }
    });
    ByteBuffer byteBuffer = DirectAlignedBufferAllocator.allocateAlignedByteBuffer(length, ailgn);
    testWrite(asyncFileStorage, byteBuffer, count, length);
    asyncFileStorage.close();
  }

  private void generateDataByOffset(ByteBuffer buffer, int size, long offset) {
    int position = buffer.position();

    for (int i = 0; i < size / Long.BYTES; i++) {
      buffer.putLong(offset + (i * Long.BYTES));
    }

    buffer.position(position);
  }

  private boolean checkDataByOffset(ByteBuffer data, long offset) {
    ByteBuffer duplicate = data.duplicate();
    for (int i = 0; i < duplicate.remaining() / Long.BYTES; i++) {
      long value = duplicate.getLong();
      if (value != (offset + (i * Long.BYTES))) {
        logger.error("originValue: {}, expected: {}, index: {}", value, offset + i, i);
        return false;
      }
    }

    return true;
  }
}
