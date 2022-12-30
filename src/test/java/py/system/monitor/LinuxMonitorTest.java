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

package py.system.monitor;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.processmanager.utils.PmUtils;
import py.test.TestBase;

public class LinuxMonitorTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(LinuxMonitorTest.class);

  @Override
  public void init() throws Exception {
    super.init();
  }

  @Test
  public void testParsingProcessTable() throws Exception {
    boolean beFound = false;
    LinuxMonitor monitor;
    ProcessInfo[] processInfos;

    monitor = new LinuxMonitor();
    processInfos = monitor.processTable();
    for (ProcessInfo processInfo : processInfos) {
      if (processInfo.getPid() == PmUtils.getCurrentProcessPid()) {
        logger.debug("My process info: {}", processInfo);

        beFound = true;
        Assert.assertEquals(processInfo.getCwd(), System.getProperty("user.dir"));
      }
    }

    Assert.assertTrue(beFound);
  }
}
