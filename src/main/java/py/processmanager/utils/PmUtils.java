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

package py.processmanager.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PmUtils {
  private static final Log logger = LogFactory.getLog(PmUtils.class);

  private static final String LAUNCH_FAILED = "failed";

  private static final String LAUNCH_SUCCEEDED = "succeeded";

  public static boolean isActive(Process process) {
    if (process == null) {
      return false;
    }
    try {
      process.exitValue();
      return false;
    } catch (Exception e) {
      return true;
    }
  }

  public static void outputFailure() {
    System.out.println();
    System.out.println(LAUNCH_FAILED);
  }

  public static void outputOk() {
    System.out.println();
    System.out.println(LAUNCH_SUCCEEDED);
  }

  public static boolean processStartResults(BufferedReader reader) {
    if (reader == null) {
      logger.error("Reader is null");
      return false;
    }

    String result = null;
    boolean succeeded = false;
    do {
      try {
        logger.debug("reading the processing result of process start");
        result = reader.readLine();
      } catch (IOException e) {
        logger.error("Failed to read process builder results", e);
        break;
      }

      if (result == null) {
        logger.error(
            "the start results can't be null before \"failed\" or \"succeeded\" strings are "
                + "returned");
        break;
      }

      if (result.equals(LAUNCH_SUCCEEDED)) {
        logger.debug("Successfully start process");
        succeeded = true;
        break;
      } else if (result.equals(LAUNCH_FAILED)) {
        logger.error("Failed to start process");
        break;
      } else {
        logger.warn("the process builder returns unknown string:" + result);

      }
    } while (true);

    try {
      reader.close();
    } catch (IOException e) {
      logger.warn("can't close the input reader reading results from sub-process", e);
    }
    return succeeded;
  }

  public static int getCurrentProcessPid() {
    String pidStr = ManagementFactory.getRuntimeMXBean().getName();
    if (pidStr.split("@").length == 2) {
      return Integer.valueOf(pidStr.split("@")[0]);
    }
    return 0;
  }

  public static int getProcessPidByProcess(Process process) {
    int pid = 0;
    if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
      try {
        Field field = process.getClass().getDeclaredField("pid");
        field.setAccessible(true);
        pid = field.getInt(process);
      } catch (Throwable e) {
        logger.error("failed to get process pid", e);
      }
    }
    return pid;
  }

  public static boolean isProcessActive(Process process) {
    if (process != null) {
      try {
        process.exitValue();
      } catch (Exception e) {
        return true;
      }
    }
    return false;
  }

  public static boolean isProcessActive(int pid) {
    String command = "ps -p " + pid;
    logger.debug(command);
    try {
      Process process = Runtime.getRuntime().exec(command);
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = stdInput.readLine();
      while (line != null) {
        line = stdInput.readLine();
        logger.debug(line);

        if (line != null && line.contains(String.valueOf(pid))) {
          return true;
        }
      }
    } catch (IOException e) {
      logger.warn("Caught an exception when waiting for process to be shutdown", e);
    }
    return false;
  }

}
