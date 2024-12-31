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
