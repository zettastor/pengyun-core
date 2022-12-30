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

package py.scheduler;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.Test;
import py.consumer.AbstractSortedConsumerService;
import py.test.TestBase;

public class ConsumerServiceTest extends TestBase {
  public ConsumerServiceTest() throws Exception {
    init();
  }

  @Test
  public void blockingQueue() throws Exception {
    BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
    List<Integer> collection = new ArrayList<Integer>();
    queue.drainTo(collection);
    assertTrue(collection.isEmpty());
    queue.add(1);
    collection.add(queue.take());
    assertTrue(collection.size() == 1);

    queue.add(2);
    queue.add(3);

    queue.drainTo(collection);
    assertTrue(collection.size() == 3);
  }

  @Test
  public void startAndStopSortedScheduler() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    SortedQueue sortedQueue = new SortedQueue(latch);
    sortedQueue.start();

    Random random = new Random();
    int value = random.nextInt();
    sortedQueue.submit(new Integer(value));
    while (sortedQueue.values.size() != 1) {
      Thread.sleep(100);
    }

    assertTrue(sortedQueue.values.get(0) == value);

    int count = 1000;
    List<Integer> tmp = new ArrayList<Integer>();
    for (int i = 0; i < count; i++) {
      tmp.add(i);
    }

    Collections.shuffle(tmp);
    logger.info("put before, list={}", tmp);
    sortedQueue.submit(tmp);

    latch.countDown();
    sortedQueue.stop();
    assertTrue(sortedQueue.values.get(sortedQueue.values.size() - 1) == Integer.MAX_VALUE);
  }

  @Test
  public void sortedQueue() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    SortedQueue sortedQueue = new SortedQueue(latch);
    sortedQueue.start();
    Random random = new Random();
    int value = random.nextInt();
    sortedQueue.submit(new Integer(value));
    while (sortedQueue.values.size() != 1) {
      Thread.sleep(100);
    }

    assertTrue(sortedQueue.values.get(0) == value);

    int count = 1000;
    List<Integer> tmp = new ArrayList<Integer>();
    for (int i = 0; i < count; i++) {
      tmp.add(i);
      if (i > 100) {
        latch.countDown();
      }
    }

    Collections.shuffle(tmp);
    logger.info("put before, list={}", tmp);
    sortedQueue.submit(tmp);

    while (sortedQueue.values.size() != 1001) {
      Thread.sleep(100);
    }

    assertTrue(sortedQueue.values.get(sortedQueue.values.size() - 1) == sortedQueue.maxValue);
  }

  private class SortedQueue extends AbstractSortedConsumerService<Integer> {
    public CountDownLatch latch;
    public List<Integer> values;
    public Integer maxValue;

    public SortedQueue(CountDownLatch latch) {
      super(new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
          if (o1 > o2) {
            return 1;
          } else if (o1 == o2) {
            return 0;
          } else {
            return -1;
          }
        }
      }, "Test");
      this.latch = latch;
      this.values = new ArrayList<Integer>();
    }

    @Override
    public void consume(Collection<Integer> objects) {
      logger.info("schedule={}", objects);
      for (Integer value : objects) {
        if (value.intValue() == Integer.MAX_VALUE) {
          logger.info("exit======");
          shutdown();
        }

        maxValue = value;
      }
      values.addAll(objects);

      try {
        latch.await();
      } catch (InterruptedException e) {
        logger.error("caught exception", e);
      }
    }

    @Override
    public void stop() {
      submit(Integer.MAX_VALUE);
      super.join();
    }
  }
}
