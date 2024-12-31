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

package py.asyncio;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.commons.lang.Validate;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.storage.PyOsInfo;
import py.storage.PyOsInfo.Os;
import py.test.TestBase;

public class PyOsInfoTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(AsyncFileStorageTest.class);

  @Test
  public void testLinux() throws Exception {
    String osName = executeCommand("uname -s");

    osName = osName.toLowerCase();

    logger.warn("osName:{}", osName);
    Os os = PyOsInfo.getOs();

    logger.warn("os:{} version:{}", os, os.getVersion());
    if (osName.contains("linux")) {
      Validate.isTrue(os.equals(Os.LINUX));
    } else if (osName.equals("darwin")) {
      Validate.isTrue(os.equals(Os.MAC));
    } else {
      Validate.isTrue(false);
    }
  }

  private String executeCommand(String command) {
    StringBuffer output = new StringBuffer();

    Process p;
    try {
      p = Runtime.getRuntime().exec(command);
      p.waitFor();
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

      String line = "";
      while ((line = reader.readLine()) != null) {
        output.append(line);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return output.toString();

  }
}
