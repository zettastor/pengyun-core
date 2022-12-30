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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;
import py.common.Utils;
import py.common.struct.LimitQueue;
import py.common.struct.PyRingBuffer;

public class PyRingBufferTest extends TestBase {
  @Test
  public void testPyRingBuffer1() {
    int limit = RandomUtils.nextInt(20) + 10;
    PyRingBuffer<Integer> ringBuffer = new PyRingBuffer(limit);
    int appendCount = RandomUtils.nextInt(100) + limit;

    for (int i = 0; i < appendCount; i++) {
      ringBuffer.offer(i);
    }

    for (int j = (appendCount - limit); j < appendCount; j++) {
      Integer value = ringBuffer.next();
      assertNotNull(value);
      assertEquals(j, value.intValue());
    }

    assertNull(ringBuffer.next());

  }

  @Test
  public void testPyRingBuffer2() {
    int limit = 10;
    PyRingBuffer<Integer> ringBuffer = new PyRingBuffer<>(limit);
    int appendCount1 = 5;

    for (int i = 0; i < appendCount1; i++) {
      ringBuffer.offer(i);
    }

    assertEquals(0, ringBuffer.getFirst().intValue());
    assertEquals(appendCount1 - 1, ringBuffer.getLast().intValue());

    int appendCount2 = 10;

    for (int j = appendCount1; j < appendCount1 + appendCount2; j++) {
      ringBuffer.offer(j);
    }

    assertEquals(appendCount1, ringBuffer.getFirst().intValue());
    assertEquals(appendCount1 + appendCount2 - 1, ringBuffer.getLast().intValue());
  }

  @Test
  public void testPyRingBuffer3() {
    int limit = 10;
    PyRingBuffer<Integer> ringBuffer = new PyRingBuffer<>(limit);
    int appendCount1 = limit - 1;

    for (int i = 0; i < appendCount1; i++) {
      ringBuffer.offer(i);
    }

    assertEquals(appendCount1, ringBuffer.size());
    assertTrue(!ringBuffer.isFull());

    int appendCount2 = 10;

    for (int j = appendCount1; j < appendCount1 + appendCount2; j++) {
      ringBuffer.offer(j);
      assertTrue(ringBuffer.isFull());
    }

    assertEquals(limit, ringBuffer.size());
  }

  @Test
  public void testPyRingBuffer4() {
    int limit = 10;
    PyRingBuffer<Integer> ringBuffer = new PyRingBuffer<>(limit);
    int appendCount1 = 5;

    for (int i = 0; i < appendCount1; i++) {
      ringBuffer.offer(i);
    }

    assertEquals(appendCount1, ringBuffer.size());

    int appendCount2 = 10;

    for (int j = appendCount1; j < appendCount1 + appendCount2; j++) {
      ringBuffer.offer(j);
    }

    assertEquals(limit, ringBuffer.size());

    String buildString = ringBuffer.buildString();

    assertNotNull(buildString);

    int appearCount = Utils.appearCount(buildString, PyRingBuffer.ELEMENT_IDENTIFIER);
    assertEquals(limit, appearCount);

  }

  @Test
  public void testMultiThreadUsePyRingBuffer() {
    int threadCount = 128;
    PyRingBuffer<Integer> ringBuffer = new PyRingBuffer<>(threadCount);
    int loopCount = 128 * 4;
    CountDownLatch threadExeLatch = new CountDownLatch(1);
    CountDownLatch threadEndLatch = new CountDownLatch(threadCount);
    AtomicInteger appendCount = new AtomicInteger(0);

    class TestThread extends Thread {
      @Override
      public void run() {
        try {
          threadExeLatch.await();
          for (int i = 0; i < loopCount; i++) {
            Thread.sleep(1);
            ringBuffer.offer(i);
            appendCount.incrementAndGet();
          }
        } catch (Exception e) {
          fail();
        } finally {
          threadEndLatch.countDown();
        }

      }
    }

    for (int j = 0; j < threadCount; j++) {
      TestThread thread1 = new TestThread();
      thread1.start();
    }

    threadExeLatch.countDown();
    try {
      threadEndLatch.await();
    } catch (Exception e) {
      fail();
    }
    assertEquals(threadCount, ringBuffer.size());
    AtomicInteger readCount = new AtomicInteger(0);
    int maxValueShowUpCount = 0;
    while (true) {
      Integer value = ringBuffer.next();
      if (value == null) {
        break;
      } else {
        if (value.intValue() == loopCount - 1) {
          maxValueShowUpCount++;
        }
        readCount.incrementAndGet();
      }
    }

    assertTrue(maxValueShowUpCount > 0);

    assertEquals(threadCount * loopCount, appendCount.get());
  }

  @Ignore
  @Test
  public void testMultiThreadUseLimitQueue1() {
    int threadCount = 128;

    LimitQueue<Integer> newLimitQueue = new LimitQueue<>(threadCount);
    int loopCount = 128 * 4;
    CountDownLatch threadExeLatch = new CountDownLatch(1);
    CountDownLatch threadEndLatch = new CountDownLatch(threadCount);
    AtomicInteger appendCount = new AtomicInteger(0);

    class TestThread extends Thread {
      @Override
      public void run() {
        try {
          threadExeLatch.await();
          for (int i = 0; i < loopCount; i++) {
            Thread.sleep(1);
            newLimitQueue.offer(i);
            appendCount.incrementAndGet();
          }
        } catch (Exception e) {
          fail();
        } finally {
          threadEndLatch.countDown();
        }

      }
    }

    for (int j = 0; j < threadCount; j++) {
      TestThread thread1 = new TestThread();
      thread1.start();
    }

    threadExeLatch.countDown();
    try {
      threadEndLatch.await();
    } catch (Exception e) {
      fail();
    }
    int readCount = 0;
    int maxValueShowUpCount = 0;
    Iterator<Integer> iterator = newLimitQueue.getQueue().iterator();
    while (iterator.hasNext()) {
      Integer value = iterator.next();
      if (value == null) {
        break;
      } else {
        readCount++;

        assertTrue(value.intValue() > loopCount - threadCount / 2);
        if (value.intValue() == loopCount - 1) {
          maxValueShowUpCount++;
        }
      }
    }

    assertTrue(maxValueShowUpCount > 0);

    assertTrue(readCount >= threadCount);
    assertEquals(threadCount * loopCount, appendCount.get());
  }

}
