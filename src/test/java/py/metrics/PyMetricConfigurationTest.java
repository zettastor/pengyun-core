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

import static org.junit.Assert.assertTrue;

import com.codahale.metrics.Counter;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import py.test.TestBase;

public class PyMetricConfigurationTest extends TestBase {
  AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

  @Override
  @Before
  public void init() {
    context.getEnvironment()
        .setActiveProfiles("metric.csv", "metric.console", "metric.slf4j", "metric.jmx");
    context.register(PyMetricConfigruation.class);
    context.refresh();
  }

  @Test
  public void testPyMetricConfigruation() throws InterruptedException {
    File csvFile = new File(String.format("%s/counter.csv", "/tmp"));
    if (csvFile.exists()) {
      csvFile.delete();
    }

    PyMetric counter = PyMetricRegistry.getMetricRegistry().register("counter", Counter.class);
    for (int i = 0; i < 6; i++) {
      counter.incCounter();
      Thread.sleep(2000);
    }

    PyMetricRegistry.getMetricRegistry().stopReporter();

    for (int i = 0; i < 6; i++) {
      counter.incCounter();
      Thread.sleep(2000);
    }

    assertTrue(csvFile.exists());

  }
}
