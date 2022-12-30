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

package py.engine;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import py.test.TestBase;

public class DelayedTaskEngineTest extends TestBase {
  private DelayedTaskEngine engine;

  public DelayedTaskEngineTest() {
    engine = new DelayedTaskEngine();
  }

  @Before
  public void beforeMethod() {
    engine.start();
  }

  @After
  public void afterMethod() {
    try {
      assertEquals(engine.getPendingTask(), 0);
    } finally {
      engine.stop();
    }
  }

  @Test
  public void test() throws Exception {
    int count = 100;
    DelayedTask[] beforeTasks = new DelayedTask[count];
    DelayedTask[] afterTasks = new DelayedTask[count];
    for (int i = 0; i < count; i++) {
      final int index = i;
      DelayedTask task = new DelayedTask(i) {
        @Override
        public Result work() {
          logger.warn("current task={}", this);
          afterTasks[index] = this;
          return null;
        }
      };
      beforeTasks[i] = task;
      engine.drive(task);
    }

    Thread.sleep(2 * count);
    for (int i = 0; i < count; i++) {
      assertEquals(beforeTasks[i], afterTasks[i]);
    }
  }

  @Test
  public void testTaskCancle() throws Exception {
    AtomicInteger integ = new AtomicInteger(0);

    DelayedTask[] tasks = new DelayedTask[10];

    for (int i = 0; i < 10; i++) {
      DelayedTask delayedTask = new DelayedTask(6000) {
        @Override
        public Result work() {
          if (isCancel()) {
            logger.warn("the task is be cancled");
          }
          integ.incrementAndGet();
          return null;
        }
      };
      tasks[i] = delayedTask;
      engine.drive(delayedTask);
    }

    for (int i = 0; i < 10; i++) {
      tasks[i].cancel();
    }

    Thread.sleep(10000);

    logger.warn("the integ num is {}", integ.get());
    assertEquals(integ.get(), 0);
  }

  @Test
  public void testTaskOverCancle() throws Exception {
    AtomicInteger integ = new AtomicInteger(0);

    DelayedTask[] tasks = new DelayedTask[10];

    for (int i = 0; i < 10; i++) {
      DelayedTask delayedTask = new DelayedTask(1) {
        @Override
        public Result work() {
          if (isCancel()) {
            logger.warn("the task is be cancled");
          }
          integ.incrementAndGet();
          return null;
        }
      };
      tasks[i] = delayedTask;
      engine.drive(delayedTask);
    }

    Thread.sleep(100);
    for (int i = 0; i < 10; i++) {
      tasks[i].cancel();
    }
    logger.warn("the integ num is {}", integ.get());
    assertEquals(integ.get(), 10);
  }
}

