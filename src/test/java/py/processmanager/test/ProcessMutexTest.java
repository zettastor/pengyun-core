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

package py.processmanager.test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.processmanager.ProcessManagerMutex;
import py.test.TestBase;

public class ProcessMutexTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(ProcessMutexTest.class);
  final int threadsNumber = 2;
  private boolean isOneMutex = false;
  private AtomicInteger count = new AtomicInteger();

  @Override
  public void init() throws Exception {
    super.init();
    super.setLogLevel(Level.DEBUG);
  }

  @Test
  public void testName() throws Exception {
    CountDownLatch threadLatch = new CountDownLatch(threadsNumber);
    for (int i = 0; i < threadsNumber; i++) {
      new Thread() {
        public void run() {
          synchronized (new Object()) {
            try {
              if (ProcessManagerMutex.checkIfAlreadyRunning(System.getProperty("user.dir"))) {
                isOneMutex = true;
                count.incrementAndGet();

              }

            } catch (IOException e) {
              e.printStackTrace();
            } finally {
              threadLatch.countDown();
            }
          }
        }
      }.start();

    }
    threadLatch.await();
    Assert.assertTrue(isOneMutex);
    Assert.assertEquals(count.get(), 1);

  }

}
