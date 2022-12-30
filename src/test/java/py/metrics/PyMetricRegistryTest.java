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

package py.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import java.util.SortedMap;
import java.util.concurrent.Callable;
import org.junit.Assert;
import org.junit.Test;
import py.monitor.jmx.server.ResourceType;
import py.test.TestBase;

public class PyMetricRegistryTest extends TestBase {
  @Override
  public void init() throws Exception {
    super.init();
  }

  @Test
  public void testDisabledRegister() {
    PyMetricRegistry registry = PyMetricRegistry.getMetricRegistry().setEnableMetric(false);
    PyMetric metric = registry.register("a", Counter.class);
    assertTrue(metric instanceof PyNullMetric);
  }

  @Test
  public void testRegisterCounter() throws InterruptedException {
    PyMetricRegistry registry = PyMetricRegistry.getMetricRegistry().setEnableMetric(true);
    PyMetric metric = registry.register("b", Counter.class);
    assertTrue(metric instanceof PyMetricImpl);
    metric.incCounter();
    SortedMap<String, Counter> counters = registry.getCounters();
    Counter c = counters.get("b");
    assertEquals(1, c.getCount());
    metric.incCounter();
    assertEquals(2, c.getCount());
    metric.decCounter();
    assertEquals(1, c.getCount());
  }

  @Test
  public void testRegisterTimer() {
    PyMetricRegistry registry = PyMetricRegistry.getMetricRegistry().setEnableMetric(true);
    PyMetric metric = registry.register("c", Timer.class);
    assertTrue(metric instanceof PyMetricImpl);
    Callable<Integer> callable = new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        Thread.sleep(1000);
        return 0;
      }
    };
    try {
      metric.time(callable);
    } catch (Exception e) {
      fail();
    }

    SortedMap<String, Timer> timers = registry.getTimers();
    Timer t = timers.get("c");
    assertEquals(1, t.getCount());
    logger.debug("15 min rate {} ", t.getFifteenMinuteRate());
    logger.debug("1 min rate {} ", t.getOneMinuteRate());
    logger.debug("mean rate {}", t.getMeanRate());
    logger.debug("Mean {} ", t.getSnapshot().getMean());

  }

  @Test
  public void testCreateMetricNameEx() throws Exception {
    PyMetric readUnitCount;
    PyMetricRegistry metricRegistry = PyMetricRegistry.getMetricRegistry();
    metricRegistry.setEnableMetric(true);
    readUnitCount = metricRegistry
        .register(PyMetricRegistry.nameEx("PYMetricRegistryTest", "testNameEx").build(),
            Counter.class);

    String metricName = PyMetricRegistry
        .nameEx(PyMetricRegistryTest.class.getSimpleName(), "testNameEx")
        .type(ResourceType.VOLUME).id("1234").build();
    logger.debug("{}", metricName);
    Assert.assertEquals("PyMetricRegistryTest.testNameEx.__<<TYPE[VOLUME] ID[1234]>>", metricName);
  }
}
