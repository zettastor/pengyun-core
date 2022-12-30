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
