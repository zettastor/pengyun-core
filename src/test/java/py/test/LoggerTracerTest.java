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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static py.common.LoggerTracer.ONE_OBJECT_MAX_MARK_NUMBER;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import py.common.LoggerTracer;
import py.common.RequestIdBuilder;
import py.common.TraceAction;

public class LoggerTracerTest extends TestBase {
  @BeforeClass
  public static void startDisruptor() {
    LoggerTracer.getInstance().start();
    LoggerTracer.getInstance().enableLoggerTracer();
  }

  @Test
  public void testLogTracer() throws InterruptedException {
    LoggerTracer loggerTracer = LoggerTracer.getInstance();

    Long requestId = RequestIdBuilder.get();
    int markCount = 0;

    markCount++;
    loggerTracer.mark(requestId, this.getTestClassName(), "just for test-0");

    markCount++;
    loggerTracer.mark(requestId, this.getTestClassName(), "just for test-1", new Exception());

    markCount++;
    loggerTracer.mark(requestId, this.getTestClassName(), "just:{} for test-2", requestId);
    markCount++;
    loggerTracer
        .mark(requestId, this.getTestClassName(), "just:{} for test-3", requestId, new Exception());

    markCount++;
    loggerTracer.mark(requestId, this.getTestClassName(), "just:{} for test-4:{}", requestId, 1);
    markCount++;
    loggerTracer.mark(requestId, this.getTestClassName(), "just:{} for test-5:{}", requestId, 1,
        new Exception());

    markCount++;
    loggerTracer
        .mark(requestId, this.getTestClassName(), "just:{} for test-6:{}, {}", requestId, 2, 2);
    markCount++;
    loggerTracer
        .mark(requestId, this.getTestClassName(), "just:{} for test-7:{}, {}", requestId, 3, 3,
            new Exception());
    Thread.sleep(100);
    assertEquals(markCount, loggerTracer.getLoggerCount(requestId));

    loggerTracer.doneTrace(requestId, TraceAction.Write, 0);
    Thread.sleep(LoggerTracer.DONE_TRACE_DELAY_TIME_MS + 100);
    assertEquals(0, loggerTracer.getLoggerCount(requestId));
  }

  @Test
  public void testLogTracer1() throws InterruptedException {
    int markCount = ONE_OBJECT_MAX_MARK_NUMBER + 10;
    LoggerTracer loggerTracer = LoggerTracer.getInstance();
    Long requestId = RequestIdBuilder.get();
    for (int i = 0; i < markCount; i++) {
      loggerTracer.mark(requestId, this.getTestClassName(), "just for test");
    }

    Thread.sleep(100);
    assertTrue(ONE_OBJECT_MAX_MARK_NUMBER <= loggerTracer.getLoggerCount(requestId));
    loggerTracer.doneTrace(requestId, TraceAction.Write, 0);
    Thread.sleep(LoggerTracer.DONE_TRACE_DELAY_TIME_MS + 100);
    assertEquals(0, loggerTracer.getLoggerCount(requestId));
  }

  @Test
  public void testLogTracer2() throws InterruptedException {
    int markCount = ONE_OBJECT_MAX_MARK_NUMBER + 10;
    LoggerTracer loggerTracer = LoggerTracer.getInstance();
    Long requestId = RequestIdBuilder.get();
    for (int i = 0; i < markCount; i++) {
      loggerTracer.mark(requestId, this.getTestClassName(), "just for test");
    }
    Thread.sleep(100);
    assertTrue(ONE_OBJECT_MAX_MARK_NUMBER <= loggerTracer.getLoggerCount(requestId));
    loggerTracer.doneTrace(requestId, TraceAction.Write, 0);
    Thread.sleep(LoggerTracer.DONE_TRACE_DELAY_TIME_MS + 100);
    assertEquals(0, loggerTracer.getLoggerCount(requestId));
  }

  @Ignore
  @Test
  public void testStartAndStop() throws InterruptedException {
    LoggerTracer.getInstance().stop();
    Long requestId = RequestIdBuilder.get();

    LoggerTracer.getInstance().start();

    LoggerTracer.getInstance().mark(requestId, this.getTestClassName(), "just for test");
    Thread.sleep(20);
    assertEquals(0, LoggerTracer.getInstance().getLoggerCount(requestId));
    LoggerTracer.getInstance().stop();
  }

  @Test
  public void testDisableLoggerTracer() throws InterruptedException {
    LoggerTracer.getInstance().disableLoggerTracer();
    Long requestId = RequestIdBuilder.get();

    LoggerTracer.getInstance().mark(requestId, this.getTestClassName(), "just for test");
    Thread.sleep(20);
    assertEquals(0, LoggerTracer.getInstance().getLoggerCount(requestId));
    LoggerTracer.getInstance().enableLoggerTracer();
  }

  @Test
  public void testMultiThreadUseLoggerTracer() throws Exception {
    LoggerTracer.getInstance().enableLoggerTracer();
    int loggerCount = 110;
    int threadCount = 128;
    CountDownLatch threadExeLatch = new CountDownLatch(1);
    CountDownLatch threadEndLatch = new CountDownLatch(threadCount);
    AtomicInteger markCount = new AtomicInteger(0);

    class UseLoggerTracerThread extends Thread {
      private long oriId;
      private String threadName;

      public UseLoggerTracerThread(long oriId) {
        this.oriId = oriId;
        this.threadName = String.valueOf(oriId);
      }

      @Override
      public void run() {
        try {
          threadExeLatch.await();
          for (int i = 0; i < loggerCount; i++) {
            LoggerTracer.getInstance()
                .mark(this.oriId, this.threadName, "just mark ori:{} at:>>{}<<", this.oriId, i);
            markCount.incrementAndGet();
          }
          LoggerTracer.getInstance().doneTrace(this.oriId, TraceAction.Write, 0);
          Thread.sleep(LoggerTracer.DONE_TRACE_DELAY_TIME_MS + 100);
        } catch (Exception e) {
          fail();
        } finally {
          threadEndLatch.countDown();
        }
      }
    }

    for (int j = 0; j < threadCount; j++) {
      UseLoggerTracerThread thread1 = new UseLoggerTracerThread(j);
      thread1.start();
    }

    threadExeLatch.countDown();
    try {
      threadEndLatch.await();
    } catch (Exception e) {
      fail();
    }

    assertEquals(loggerCount * threadCount, markCount.get());

    for (int j = 0; j < threadCount; j++) {
      assertEquals(0, LoggerTracer.getInstance().getLoggerCount((long) j));
    }

  }

}
