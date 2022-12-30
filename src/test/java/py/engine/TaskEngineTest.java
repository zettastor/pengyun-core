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

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang.Validate;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import py.periodic.UnableToStartException;
import py.test.TestBase;
import py.token.controller.TokenControllerCenter;
import py.token.controller.TokenControllerUtils;

public class TaskEngineTest extends TestBase {
  public TaskEngineTest() throws Exception {
    super.init();
  }

  @BeforeClass
  public static void beforeClass() {
    logger.info("before class");
    try {
      TokenControllerCenter.getInstance().start();
    } catch (UnableToStartException e) {
      e.printStackTrace();
    }
  }

  @AfterClass
  public static void afterClass() {
    TokenControllerCenter.getInstance().stop();
    logger.info("after class");
  }

  @Test
  public void singleEngineStartAndStop() throws Exception {
    SingleTaskEngine engine = new SingleTaskEngine(0);
    engine.start();
    engine.drive(generateDefaultTask(AbstractTask.DEFAULTLISTENER));
    Thread.sleep(100);
    assertTrue(engine.getPendingTask() == 0);
    engine.stop();

    engine.drive(generateDefaultTask(AbstractTask.DEFAULTLISTENER));

    assertTrue(engine.getPendingTask() == 1);
    Thread.sleep(100);
    assertTrue(engine.getPendingTask() == 1);
  }

  @Test
  public void singleEngineTimeout() throws Exception {
    int maxPendingTask = 100;
    int iops = 100;
    SingleTaskEngine engine = new SingleTaskEngine(maxPendingTask);
    engine.setMaxDrainTo(1);
    engine.setTokenController(TokenControllerUtils.generateAndRegister(iops));
    engine.start();
    final AtomicInteger submitCount = new AtomicInteger(0);
    TaskListener listener = new TaskListener() {
      @Override
      public void response(Result result) {
        submitCount.incrementAndGet();
      }
    };

    int count = 0;
    boolean timeout = false;
    while (count < maxPendingTask * 5) {
      if (!engine.drive(genenrateDelayTask(0, listener), 200, TimeUnit.MILLISECONDS)) {
        logger.info("timeout, current count: {}", count);
        timeout = true;
        break;
      } else {
        count++;
      }
    }

    logger.info("count: {}, submitCount: {}", count, submitCount.get());
    assertTrue(timeout);
    logger.info("now stop begine");
    engine.stop();
    logger.info("now stop end");
    assertTrue(count == submitCount.get());
  }

  @Test
  public void singleEngineControl() throws Exception {
    int iops = 100;
    SingleTaskEngine engine = new SingleTaskEngine(2000);
    engine.setMaxDrainTo(Integer.MAX_VALUE);
    engine.setTokenController(TokenControllerUtils.generateAndRegister(iops));
    engine.start();
    final AtomicInteger submitCount = new AtomicInteger(0);
    TaskListener listener = new TaskListener() {
      @Override
      public void response(Result result) {
        logger.info("result: {} for task", result);
        submitCount.incrementAndGet();
      }
    };

    int times = 4;
    int taskCount = iops * times;
    logger.info("drive data");

    long startTime = System.currentTimeMillis();
    for (int i = 0; i < taskCount; i++) {
      assertTrue(engine.drive(generateDefaultTask(listener)));
    }

    long costTime = 0;
    while ((costTime = System.currentTimeMillis() - startTime) < 4000) {
      if (taskCount == submitCount.get()) {
        break;
      } else {
        Thread.sleep(20);
      }
    }

    logger.warn("cost time: {}", costTime);
    Validate.isTrue(costTime <= 4000);
    engine.stop();
  }

  private Task generateDefaultTask(TaskListener listener) {
    return new AbstractTask(listener) {
      @Override
      public Result work() {
        return new ResultImpl();
      }
    };
  }

  private Task genenrateDelayTask(int delayMs, TaskListener listener) {
    return new AbstractTask(listener) {
      @Override
      public Result work() {
        try {
          Thread.sleep(delayMs);
        } catch (InterruptedException e) {
          logger.error("caught exception", e);
        }
        return new ResultImpl();
      }
    };
  }
}
