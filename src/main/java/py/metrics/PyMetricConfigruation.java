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

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.SlidingTimeWindowArrayReservoir;
import com.codahale.metrics.ganglia.GangliaReporter;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.jmx.JmxReporter;
import info.ganglia.gmetric4j.gmetric.GMetric;
import info.ganglia.gmetric4j.gmetric.GMetric.UDPAddressingMode;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource({"classpath:config/metric.properties"})
public class PyMetricConfigruation {
  private static final Logger logger = LoggerFactory.getLogger(PyMetricConfigruation.class);

  @Value("${metric.enable:false}")
  private boolean enableMetric = false;

  @Value("${use.sliding.window.reservoir:false}")
  private boolean useSlidingWindowReservoir = false;

  @Value("${sliding.window.size.ms:20000}")
  private int slidingWindowSizeMs = 20000;

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  public PyMetricRegistry pyMetricRegistry() {
    PyMetricRegistry.initRegister(reservoirSupplier());
    PyMetricRegistry pyMetricRegistry = PyMetricRegistry.getMetricRegistry();
    pyMetricRegistry.setEnableMetric(enableMetric);
    return pyMetricRegistry;
  }

  @Bean
  public Supplier<Reservoir> reservoirSupplier() {
    if (useSlidingWindowReservoir) {
      return () -> new SlidingTimeWindowArrayReservoir(slidingWindowSizeMs,
          TimeUnit.MILLISECONDS);
    } else {
      return ExponentiallyDecayingReservoir::new;
    }
  }

  @Configuration
  @Profile({"metric.ganglia"})
  public static class GangliaReporerConfiguration {
    @Value("${metric.ganglia.port:8649}")
    private int gangliaPort = 8649;

    @Value("${metric.ganglia.interval.seconds:10}")
    private long interval = 10;

    @Value("${metric.ganglia.group.name:localhost}")
    private String groupName = "localhost";

    @Value("${metric.ganglia.switch:false}")
    private boolean gangliaSwitch = false;

    @Value("${metric.ganglia.filter.regex:}")
    private String filterRegex = "";

    @Autowired
    private PyMetricRegistry metrics;

    @Bean
    public MetricFilter metricFilter() {
      PyMetricReporterFilter.Ganglia.compilePattern(filterRegex);
      return PyMetricReporterFilter.Ganglia;
    }

    @Bean
    public GangliaReporter gangliaReporter() {
      if (!gangliaSwitch) {
        return null;
      }

      GMetric ganglia = null;
      try {
        ganglia = new GMetric(groupName, gangliaPort, UDPAddressingMode.UNICAST, 1);
      } catch (IOException e) {
        logger.error("caught an exception", e);
        throw new RuntimeException(e);
      }

      logger.warn("Going to start ganglia reporter, configuration: {}", toString());
      final GangliaReporter reporter = GangliaReporter.forRegistry(metrics)
          .filter(metricFilter()).convertRatesTo(TimeUnit.SECONDS)
          .convertDurationsTo(TimeUnit.MILLISECONDS).build(ganglia);
      reporter.start(interval, TimeUnit.SECONDS);
      return reporter;
    }

    @Override
    public String toString() {
      return "GangliaReporerConfiguration [gangliaPort=" + gangliaPort + ", interval=" + interval
          + ", groupName="
          + groupName + ", metrics=" + metrics + "]";
    }

  }

  @Configuration
  @Profile({"metric.graphite"})
  public static class GraphiteReporterConfiguration {
    @Value("${metric.graphite.ip:localhost}")
    private String graphiteIp = "localhost";

    @Value("${metric.graphite.port:2003}")
    private int graphitePort = 2003;

    @Value("${metric.graphite.prefix:pengyun.local.service}")
    private String prefix = "pengyun.local.service";

    @Value("${metric.graphite.interval.seconds:10}")
    private long interval = 10;

    @Value("${metric.graphite.switch:false}")
    private boolean graphiteSwitch = false;

    @Value("${metric.graphite.filter.regex:}")
    private String filterRegex = "";

    @Autowired
    private PyMetricRegistry metrics;

    @Bean
    public Graphite graphite() {
      Graphite graphite = new Graphite(new InetSocketAddress(graphiteIp, graphitePort));
      return graphite;
    }

    @Bean
    public MetricFilter metricFilter() {
      PyMetricReporterFilter.Graphite.compilePattern(filterRegex);
      return PyMetricReporterFilter.Graphite;
    }

    @Bean
    public GraphiteReporter graphiteReporter() {
      if (!graphiteSwitch) {
        return null;
      }

      try {
        String hostname = InetAddress.getLocalHost().getHostName();
        prefix = hostname + "." + prefix;
        String id = System.getProperty("process.identifier");
        if (id != null) {
          prefix += "." + id;
        }
      } catch (Exception e) {
        logger.error("caught an exception", e);
      }

      logger.warn("Going to start graphite reporter, configuration: {}", toString());
      GraphiteReporter reporter = GraphiteReporter.forRegistry(metrics).filter(metricFilter())
          .prefixedWith(prefix).build(graphite());
      reporter.start(interval, TimeUnit.SECONDS);
      metrics.addReporter(reporter);
      return reporter;
    }

    @Override
    public String toString() {
      return "GraphiteReporterConfiguration{"
          + "graphiteIp='" + graphiteIp + '\''
          + ", graphitePort=" + graphitePort
          + ", prefix='" + prefix + '\''
          + ", interval=" + interval
          + ", graphiteSwitch=" + graphiteSwitch
          + ", filterRegex='" + filterRegex + '\''
          + ", metrics=" + metrics
          + '}';
    }
  }

  @Configuration
  @Profile({"metric.jmx"})
  public static class JmxReporterConfiguration {
    @Value("${metric.jmx.prefix:pengyun.local.service}")
    private String prefix = "pengyun.local.service";

    @Autowired
    private PyMetricRegistry metrics;

    @Bean
    public JmxReporter jmxReporter() {
      logger.debug("Going to start jmx reporter with prefix {}", prefix);
      JmxReporter reporter = JmxReporter.forRegistry(metrics).inDomain(prefix).build();
      reporter.start();
      metrics.addReporter(reporter);
      return reporter;
    }
  }

  @Configuration
  @Profile({"metric.csv"})
  public static class CsvReporterConfiguration {
    @Value("${metric.csv.interval.seconds:10}")
    private long interval = 10;

    @Value("${metric.csv.dir:/tmp/}")
    private String directory = System.getProperty("java.io.tmpdir");

    @Value("${metric.csv.filter.regex:}")
    private String filterRegex = "";

    @Autowired
    private PyMetricRegistry metrics;

    @Bean
    public File file() {
      if (System.getProperty("metric.csv.dir") != null) {
        directory = System.getProperty("metric.csv.dir");
      }

      File file = new File(directory);

      if (!file.exists()) {
        file.mkdirs();
      }
      return file;
    }

    @Bean
    public MetricFilter metricFilter() {
      PyMetricReporterFilter.CSV.compilePattern(filterRegex);
      return PyMetricReporterFilter.CSV;
    }

    @Bean
    public CsvReporter csvReporter() {
      logger.debug("Going to start csv reporter with target directory {}", directory);

      CsvReporter reporter = CsvReporter.forRegistry(metrics).filter(metricFilter()).build(file());
      reporter.start(interval, TimeUnit.SECONDS);
      metrics.addReporter(reporter);
      return reporter;
    }
  }

  @Configuration
  @Profile({"metric.console"})
  public static class ConsoleReporterConfiguration {
    @Value("${metric.console.interval.seconds:10}")
    private long interval = 10;

    @Autowired
    private PyMetricRegistry metrics;

    @Bean
    public ConsoleReporter consoleReporter() {
      logger.debug("Going to start console reporter");

      ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics).build();
      reporter.start(interval, TimeUnit.SECONDS);
      metrics.addReporter(reporter);
      return reporter;
    }
  }

  @Configuration
  @Profile({"metric.slf4j"})
  public static class Slf4jReporterConfiguration {
    @Value("${metric.slf4j.prefix:pengyun.local.service}")
    private String prefix = "pengyun.local.service";

    @Value("${metric.slf4j.interval.seconds:10}")
    private long interval = 10;

    @Autowired
    private PyMetricRegistry metrics;

    @Bean
    public Slf4jReporter slf4jReporter() {
      logger.debug("Going to start slf4j reporter with prefix {}", prefix);

      Slf4jReporter reporter = Slf4jReporter.forRegistry(metrics)
          .outputTo(LoggerFactory.getLogger(prefix))
          .build();
      reporter.start(interval, TimeUnit.SECONDS);
      metrics.addReporter(reporter);
      return reporter;
    }

  }
}
