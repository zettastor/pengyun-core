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

import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.exception.StorageException;

class AsyncFileAccessor {
  static final int ERR_SUCCESS = 0;
  static final int ERR_FAIL = -1;
  private static final Logger logger = LoggerFactory.getLogger(AsyncFileAccessor.class);

  static {
    logger.warn("load path:{}", System.getProperty("java.library.path"));
    System.loadLibrary("linux-async-io");
  }

  private final AtomicInteger readCounter;
  private final AtomicInteger writeCounter;
  private final long pyFd;
  private final Path storagePath;
  private volatile boolean isClosed;

  private AsyncFileAccessor(long pyFd, Path storagePath) {
    this.pyFd = pyFd;
    this.storagePath = storagePath;
    this.readCounter = new AtomicInteger(0);
    this.writeCounter = new AtomicInteger(0);
  }

  static AsyncFileAccessor open(Path path, int ioDepth) throws StorageException {
    long pyFd = open(path.toUri().getPath(), ioDepth);
    if (pyFd < 0) {
      logger.error("open:{} ioDepth:{}", path.toString(), ioDepth);
      throw new StorageException("open:" + path.toString() + "ioDepth:" + ioDepth);
    }
    return new AsyncFileAccessor(pyFd, path);
  }

  private static native long open(String path, int ioDepth);

  private static native void close(long pyFd);

  void close() {
    if (!isClosed) {
      isClosed = true;
      while (readCounter.get() != 0 || writeCounter.get() != 0) {
        logger.warn("storage path:{} read counter:{}  write counter:{} sleep and try again",
            storagePath,
            readCounter.get());
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
          logger.warn("sleep interrupted exception.", e);
        }
      }
      logger.warn("storage path:{} will be closed", storagePath);
      close(pyFd);
      logger.warn("storage path:{} been closed completely", storagePath);
    }
  }

  private static native void write(long pyFd, long address, long offset, int length,
      Callback callback);

  <A> void write(long address, long offset, int length, A attachment,
      CompletionHandler<Integer, A> completionHandler) {
    writeCounter.incrementAndGet();

    if (isClosed) {
      writeCounter.decrementAndGet();
      completionHandler.failed(new StorageException("file accessor has been closed !"), attachment);
      return;
    }

    try {
      write(pyFd, address, offset, length,
          new IoCallback<>(offset, length, attachment, completionHandler));
    } finally {
      writeCounter.decrementAndGet();
    }
  }

  private static native void read(long pyFd, long address, long offset, int length,
      Callback callback);

  <A> void read(long address, long offset, int length, A attachment,
      CompletionHandler<Integer, A> completionHandler) {
    readCounter.incrementAndGet();
    if (isClosed) {
      readCounter.decrementAndGet();
      completionHandler.failed(new StorageException("file accessor has been closed !"), attachment);
      return;
    }

    try {
      read(pyFd, address, offset, length,
          new IoCallback<>(offset, length, attachment, completionHandler));
    } finally {
      readCounter.decrementAndGet();
    }
  }

  private static native void enableDiskCheck(long pyFd, int seqential, int random, double quartile,
      int policy, Callback callback);

  void enableSlowDisk(int seqential, int random, double quartile, int policy, Callback callback) {
    enableDiskCheck(pyFd, seqential, random, quartile, policy, callback);
  }
}
