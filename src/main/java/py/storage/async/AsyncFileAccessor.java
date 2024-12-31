/**
* Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
