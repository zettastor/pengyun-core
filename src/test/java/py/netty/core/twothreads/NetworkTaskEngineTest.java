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

package py.netty.core.twothreads;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.Assert;
import org.junit.Test;
import py.test.TestBase;

public class NetworkTaskEngineTest extends TestBase {
  private static Runnable SHUTDOWN_TASK = new Runnable() {
    @Override
    public void run() {
    }
  };

  @Test
  public void testGracePeriod() throws Exception {
    AtomicInteger tasksInEngine = new AtomicInteger(0);
    NetworkTaskEngine engine = new NetworkTaskEngineClassForTest();

    engine.submit(new Runnable() {
      @Override
      public void run() {
        int count = tasksInEngine.incrementAndGet();
        logger.debug("count: {}", count);
      }
    });

    engine.shutdownGracefully(5, 50, TimeUnit.SECONDS);

    long startTime = System.currentTimeMillis();
    int ntasks = 10;
    for (int i = 0; i < ntasks; i++) {
      engine.submit(new Runnable() {
        @Override
        public void run() {
          int count = tasksInEngine.incrementAndGet();
          logger.debug("count: {}", count);
        }
      });
      Thread.sleep(200);
    }

    logger.debug("start to sleep 1 second");
    Thread.sleep(1000);
    logger.debug("engine has not been shutdown yet");
    Assert.assertFalse(engine.isTerminated());
    logger.debug("start to sleep 2.5 second");
    Thread.sleep(2500);
    Assert.assertTrue(engine.isTerminated());
  }

  @Test
  public void testPerformance() throws Exception {
    NetworkTaskEngine engine = new NetworkTaskEngineClassForTest();
    engine.start();

    long start = System.nanoTime();
    engine.submit(new Runnable() {
      @Override
      public void run() {
        long end = System.nanoTime();
        long duration = (end - start) / 1000;
        logger.debug("duration {}", duration);
      }
    });

    Thread.sleep(1000);
    engine.shutdownGracefully(1, 50, TimeUnit.SECONDS);
  }

  private class NetworkTaskEngineClassForTest extends
      NetworkTaskEngineUsingBlockingQueue<Runnable> {
    public NetworkTaskEngineClassForTest() {
      super(new Consumer<Runnable>() {
        @Override
        public void accept(Runnable task) {
          try {
            task.run();
          } catch (Throwable t) {
            logger.warn("The execution of the task {} ", task, t);
          }
        }
      }, "test");

    }

    @Override
    protected void enqueueShutdownTask() {
      enqueue(SHUTDOWN_TASK);
    }

    @Override
    protected boolean isShutdownTask(Runnable o) {
      return o == SHUTDOWN_TASK;
    }
  }
}
