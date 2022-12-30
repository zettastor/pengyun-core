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

import com.codahale.metrics.Timer;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import py.test.TestBase;

public class PyMetricConfigruationTest extends TestBase {
  public void testGraphiteReporter() throws InterruptedException {
    String prefix = "tyr2.mac2";
    String graphiteIp = "10.0.2.189";
    int graphitePort = 2003;
    PyMetricRegistry metrics = PyMetricRegistry.getMetricRegistry();

    Graphite graphite = new Graphite(new InetSocketAddress(graphiteIp, graphitePort));
    GraphiteReporter reporter = GraphiteReporter.forRegistry(metrics)
        .prefixedWith(prefix).build(graphite);
    reporter.start(1, TimeUnit.SECONDS);
    metrics.addReporter(reporter);

    PyMetric timer = metrics.register("hello.world", Timer.class, true);
    Random random = new Random();
    while (true) {
      PyTimerContext context = timer.time();
      Thread.sleep(random.nextInt(100));
      context.stop();
    }
  }

}