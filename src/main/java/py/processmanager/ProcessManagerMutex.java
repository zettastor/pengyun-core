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
