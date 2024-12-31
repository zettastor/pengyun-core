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
