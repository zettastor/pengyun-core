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

package py.test;

import java.util.Random;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Test;
import py.storage.impl.DeadlineTaskSelectorImpl;

public class DeadlineTaskSelectorTest extends TestBase {
  @Test
  public void testDeadlineTaskTimeoutSequential2() throws Exception {
    DeadlineTaskSelectorImpl<Integer> taskSelector = new DeadlineTaskSelectorImpl<>(
        Integer::compareTo);

    int testCount = 1024;

    long startTime = System.currentTimeMillis();
    for (int i = 0; i < testCount; i++) {
      taskSelector.offer(i, 0);
      Thread.sleep(1);

    }
    long end = System.currentTimeMillis();
    logger.warn("cost time {}", end - startTime);

    Thread consumer = new Thread(() -> {
      int index = 0;
      while (true) {
        try {
          Integer val = taskSelector.select(1, TimeUnit.SECONDS);
          if (val != null) {
            Assert.assertEquals(val.intValue(), index);
            index++;
          }

          if (index == testCount) {
            break;
          }
        } catch (InterruptedException ignore) {
          logger.error("caught exception", ignore);
        }
      }
    });

    consumer.start();
    consumer.join();
  }

  @Test
  public void testDeadlineTaskTimeoutSequentail() throws Exception {
    DeadlineTaskSelectorImpl<Integer> taskSelector = new DeadlineTaskSelectorImpl<>(
        Integer::compareTo);

    int testCount = 1024;
    Semaphore semaphore = new Semaphore(32);
    Thread producer = new Thread(() -> {
      long startTime = System.currentTimeMillis();
      for (int i = 0; i < testCount; i++) {
        taskSelector.offer(i, 0);
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      long end = System.currentTimeMillis();
      logger.warn("cost time {}", end - startTime);
    });
    Thread consumer = new Thread(() -> {
      int index = 0;
      while (true) {
        try {
          Integer val = taskSelector.select(1, TimeUnit.SECONDS);
          if (val != null) {
            Assert.assertEquals(val.intValue(), index);
            index++;
          }
        } catch (InterruptedException ignore) {
          logger.error("caught exception", ignore);
        }
      }
    });

    producer.start();
    consumer.start();
    producer.join();
  }

  public void performanceTest() throws InterruptedException {
    DeadlineTaskSelectorImpl<Integer> taskSelector = new DeadlineTaskSelectorImpl<>(
        Integer::compareTo);

    int testCount = 100000;
    Semaphore semaphore = new Semaphore(32);
    Thread producer = new Thread(() -> {
      long startTime = System.currentTimeMillis();
      for (int i = 0; i < testCount; i++) {
        try {
          semaphore.acquire();
        } catch (InterruptedException ignore) {
          logger.error("caught exception", ignore);
        }
        taskSelector.offer(i, 5);
      }
      long end = System.currentTimeMillis();
      logger.warn("cost time {}", end - startTime);
    });
    Thread consumer = new Thread(() -> {
      while (true) {
        try {
          Integer val = taskSelector.select(1, TimeUnit.SECONDS);
          if (val != null) {
            semaphore.release(1);
          }
        } catch (InterruptedException ignore) {
          logger.error("caught exception", ignore);
        }
      }
    });

    producer.start();
    consumer.start();
    producer.join();
  }

  public void testDelayedQueue() throws InterruptedException {
    DelayQueue<Delay> delayQueue = new DelayQueue<>();
    PriorityBlockingQueue<Delay> priorityBlockingQueue = new PriorityBlockingQueue<>();
    LinkedBlockingQueue<Delay> linkedBlockingQueue = new LinkedBlockingQueue<>();
    int threadCount = 12;
    Thread[] threads = new Thread[threadCount];
    int count = 1000000;
    Random random = new Random(System.currentTimeMillis());
    for (int i = 0; i < threadCount; i++) {
      Thread thread = new Thread(() -> {
        long[] times = new long[count];
        for (int j = 0; j < count; j++) {
          long startTime = System.currentTimeMillis();
          Delay delay = new Delay(System.currentTimeMillis() + random.nextInt(10000));

          linkedBlockingQueue.offer(delay);
          long endTime = System.currentTimeMillis();
          times[j] = endTime - startTime;
        }
        long sum = 0;
        for (long time : times) {
          sum += time;
        }
        logger.warn("finished, total time {} average time {}", sum, ((double) sum) / times.length);
      }, "test-thread-" + i);
      threads[i] = thread;
      thread.start();
    }
    for (Thread thread : threads) {
      thread.join();
    }
  }

  public void testDelayedQueue2() throws InterruptedException {
    class Delay implements Delayed {
      long expirationTime;

      public Delay(long expirationTime) {
        this.expirationTime = expirationTime;
      }

      @Override
      public long getDelay(@Nonnull TimeUnit unit) {
        long delayMs = expirationTime - System.currentTimeMillis();
        return unit.convert(delayMs, TimeUnit.MILLISECONDS);
      }

      @Override
      public int compareTo(@Nonnull Delayed o) {
        return Long.compare(getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
      }
    }

    DelayQueue<Delay> delayQueue = new DelayQueue<>();
    PriorityBlockingQueue<Delay> priorityBlockingQueue = new PriorityBlockingQueue<>();
    LinkedBlockingQueue<Delay> linkedBlockingQueue = new LinkedBlockingQueue<>();
    int threadCount = 12;
    Thread[] threads = new Thread[threadCount];
    int count = 1000000;
    Random random = new Random(System.currentTimeMillis());
    for (int i = 0; i < threadCount; i++) {
      Thread thread = new Thread(() -> {
        long[] times = new long[count];
        for (int j = 0; j < count; j++) {
          long startTime = System.currentTimeMillis();
          Delay delay = new Delay(System.currentTimeMillis()
              + random.nextInt(10000));
          linkedBlockingQueue.offer(delay);
          long endTime = System.currentTimeMillis();
          times[j] = endTime - startTime;
        }
        long sum = 0;
        for (long time : times) {
          sum += time;
        }
        logger.warn("finished, total time {} average time {}", sum, ((double) sum) / times.length);
      }, "test-thread-" + i);
      threads[i] = thread;
      thread.start();
    }
    for (Thread thread : threads) {
      thread.join();
    }
  }

  @Test
  public void testAlwaysExpired() throws InterruptedException {
    DeadlineTaskSelectorImpl<Integer> selector = new DeadlineTaskSelectorImpl<>(Integer::compareTo);
    selector.start();
    int max = 100;
    int offer;
    offer = max + 1;
    logger.warn("offer {}", offer);
    selector.offer(offer, 0);

    offer = max;
    logger.warn("offer {}", offer);
    selector.offer(offer, 0);

    int expect;
    expect = max;
    logger.warn("expect {}", expect);
    Assert.assertEquals(expect, (int) selector.select());

    offer = max - 1;
    logger.warn("offer {}", offer);
    selector.offer(offer, 0);

    expect = max - 1;
    logger.warn("expect {}", expect);
    Assert.assertEquals(expect, (int) selector.select());

    expect = max + 1;
    logger.warn("expect {}", expect);
    Assert.assertEquals(expect, (int) selector.select());

    Assert.assertNull(selector.select());

    for (int i = max - 1; i >= 0; i--) {
      offer = i;
      logger.warn("offer {}", offer);
      selector.offer(offer, 0);
    }

    Thread.sleep(50);
    for (int i = 0; i < max; i++) {
      expect = max - i - 1;
      logger.warn("expect {}", expect);
      Assert.assertEquals(expect, (int) selector.select());
    }
  }

  class Delay implements Delayed {
    long expirationTime;

    public Delay(long expirationTime) {
      this.expirationTime = expirationTime;
    }

    @Override
    public long getDelay(@Nonnull TimeUnit unit) {
      long delayMs = expirationTime - System.currentTimeMillis();
      return unit.convert(delayMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(@Nonnull Delayed o) {
      return Long.compare(getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }
  }

}
