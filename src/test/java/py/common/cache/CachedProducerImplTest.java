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

package py.common.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import py.common.Utils;
import py.test.TestBase;

public class CachedProducerImplTest extends TestBase {
  @Test
  public void testCachedProducer() throws Exception {
    int maxVal = 10000;
    int upperThreshold = 200;
    int lowerThreshold = 100;
    AtomicInteger executedCount = new AtomicInteger(0);
    AtomicInteger generator = new AtomicInteger(0);

    CachedProducer<Integer> cachedProducer = new CachedProducerImpl<>(
        new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>()) {
          @Override
          public void execute(Runnable command) {
            executedCount.incrementAndGet();
            logger.warn("new tasks submitted");
            super.execute(command);
          }
        }, lowerThreshold, upperThreshold, () -> {
      int val = generator.incrementAndGet();
      if (val >= maxVal) {
        throw new Exception();
      }
      return val;
    });

    Integer polledVal = null;

    polledVal = cachedProducer.poll();

    assertNotNull(polledVal);
    assertEquals(1, executedCount.get());
    try {
      Utils.waitUntilConditionMatches(1, () -> generator.get() == upperThreshold + 1);
    } catch (Exception ignore) {
      logger.error("caught exception", ignore);
    }
    assertEquals(upperThreshold + 1, generator.get());

    assertEquals(1, executedCount.get());

    for (int i = 0; i < upperThreshold - lowerThreshold - 1; i++) {
      cachedProducer.poll();
    }
    assertEquals(1, executedCount.get());

    polledVal = cachedProducer.poll();

    assertEquals(2, executedCount.get());
    try {
      Utils.waitUntilConditionMatches(1,
          () -> generator.get() == lowerThreshold + upperThreshold + 1);
    } catch (Exception ignore) {
      logger.error("caught exception", ignore);
    }
    assertEquals(lowerThreshold + upperThreshold + 1, generator.get());

    for (int i = polledVal; i < maxVal - 1; i++) {
      assertNotNull(cachedProducer.poll());
    }

    try {
      cachedProducer.poll();
      fail("an exception should have be thrown");
    } catch (Exception ignore) {
      logger.error("caught exception", ignore);
    }
  }

}