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

package py.monitor.jmx.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SigarJniLibraryCopier {
  private static final Logger logger = LoggerFactory.getLogger(SigarJniLibraryCopier.class);
  private static String rootPath = System.getProperty("user.dir");

  private SigarJniLibraryCopier() {
    logger.debug("Sigar constructor!!!!!");
    try {
      copyJniLibrary2JavaClassPath();
    } catch (Exception e) {
      logger.error("Caught an exception", e);
    }
  }

  public static SigarJniLibraryCopier getInstance() {
    return LazyHolder.singletonInstance;
  }

  public void copyJniLibrary2JavaClassPath() throws Exception {
    String[] cmdlinux = new String[3];
    cmdlinux[0] = "/bin/sh";
    cmdlinux[1] = "-c";
    cmdlinux[2] = String.format(
        "cp %s/src/test/resources/libOfMonitorJNI/* ~/.m2/repository/org/fusesource/sigar/1.6.4/",
        rootPath);
    try {
      Process pid = Runtime.getRuntime().exec(cmdlinux);
      BufferedReader reader = null;
      if (pid != null) {
        reader = new BufferedReader(new InputStreamReader(pid.getInputStream()));
        pid.waitFor();
      }

      String line = null;
      while (reader != null && (line = reader.readLine()) != null) {
        logger.debug("{}", line);
      }
    } catch (IOException e) {
      logger.error("Caught an exception", e);
      throw e;
    } catch (InterruptedException e) {
      logger.error("Caught an exception", e);
      throw e;
    }
  }

  private static class LazyHolder {
    private static final SigarJniLibraryCopier singletonInstance = new SigarJniLibraryCopier();
  }
}
