/**
* Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
