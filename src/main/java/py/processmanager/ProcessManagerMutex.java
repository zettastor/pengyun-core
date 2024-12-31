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

package py.processmanager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessManagerMutex {
  private static final Logger logger = LoggerFactory.getLogger(ProcessManagerMutex.class);
  static File file;
  static FileChannel fileChannel;
  static FileLock lock;
  private static String curDir = null;

  @SuppressWarnings("resource")
  public static boolean checkIfAlreadyRunning(String dir) throws IOException {
    curDir = dir;
    logger.warn("try to lock dir:{}", curDir);

    file = new File(curDir, "processmanagermutex.lock");
    fileChannel = new RandomAccessFile(file, "rw").getChannel();
    lock = fileChannel.tryLock();

    if (lock == null) {
      logger.warn("can not try lock at this moment, dir:{}", curDir);
      fileChannel.close();
      return false;
    }

    ShutdownHook shutdownHook = new ShutdownHook();
    Runtime.getRuntime().addShutdownHook(shutdownHook);

    logger.warn("got lock at this moment, dir:{}, and lock:{}", curDir, lock);
    return true;
  }

  public static void unlockFile() throws IOException {
    if (lock != null) {
      logger.warn("try to release lock dir:{}, lock:{}", curDir, lock);
      lock.release();
      fileChannel.close();
    }
  }

  static class ShutdownHook extends Thread {
    public void run() {
      try {
        unlockFile();
      } catch (IOException e) {
        logger.error("Caught an exception when release lock of mutext process file", e);
      }
    }
  }
}
