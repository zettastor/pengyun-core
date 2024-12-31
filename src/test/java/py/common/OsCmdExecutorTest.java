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
