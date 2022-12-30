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

package py.common;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.OsCmdExecutor.OsCmdOutputLogger;
import py.processmanager.utils.PmUtils;
import py.test.TestBase;

public class OsCmdExecutorTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(OsCmdExecutorTest.class);

  @Before
  @Override
  public void init() throws Exception {
    super.init();
    super.setLogLevel(Level.DEBUG);
  }

  @Test
  public void testCheckIfCurrentProccessExist() throws Exception {
    int currentPid = PmUtils.getCurrentProcessPid();
    String command = "ps " + currentPid;
    OsCmdOutputLogger outputLogger = new OsCmdOutputLogger(logger,
        command);
    int result = OsCmdExecutor.exec(command, outputLogger, outputLogger);
    Assert.assertEquals(0, result);
  }

  @After
  public void cleanup() {
    super.setLogLevel(Level.WARN);
  }
}
