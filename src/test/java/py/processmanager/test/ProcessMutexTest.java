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
