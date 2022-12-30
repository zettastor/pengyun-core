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

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Timer;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.monitor.jmx.server.ResourceType;

public class PyMetricRegistry extends MetricRegistry {
  private static final Logger logger = LoggerFactory.getLogger(PyMetricRegistry.class);
  private static final AtomicLong counter = new AtomicLong(0);
  private final Supplier<Reservoir> reservoirSupplier;
  private boolean enableMetric = false;
  private List<Closeable> reporterList = new ArrayList<Closeable>();

  private PyMetricRegistry(Supplier<Reservoir> reservoirSupplier) {
    this.reservoirSupplier = reservoirSupplier;
  }

  public static void initRegister(Supplier<Reservoir> reservoirSupplier) {
    LazyHolder.initRegister(reservoirSupplier);
  }

  public static PyMetricRegistry getMetricRegistry() {
    return LazyHolder.getRegister();
  }

  private static void append(StringBuilder builder, String part) {
    if (part != null && !part.isEmpty()) {
      if (builder.length() > 0) {
        builder.append('.');
      }
      builder.append(part);
    }
  }

  public static PyMetricNameBuilder nameEx(String name, String... names) {
    final StringBuilder builder = new StringBuilder();
    append(builder, name);
    if (names != null) {
      for (String s : names) {
        append(builder, s);
      }
    }
    String originalName = builder.toString();
    return new PyMetricNameBuilder(originalName);
  }

  public PyMetric registerInstance(String name, Metric metricInstance) {
    return registerInstance(name, metricInstance, enableMetric);
  }

  public PyMetric registerInstance(String name, Metric metricInstance, boolean enableMetric) {
    if (!enableMetric) {
      return PyNullMetric.defaultNullMetric;
    }

    Metric metric = null;
    try {
      metric = super.register(name, metricInstance);
    } catch (IllegalArgumentException e) {
      metric = getMetricRegistry().getMetrics().get(name);
    } catch (Exception e) {
      logger.error("Can't register name {} as metric {} due to exception", name,
          metricInstance.getClass().getName(), e);
      return null;
    }

    if (metric == null) {
      logger.error("Can't register a valid metric for use");
      return null;
    }

    if (!metric.getClass().equals(metricInstance.getClass())) {
      logger.error("Cannot register the used name {} whose type is {} as another type {} of metric",
          name,
          metric.getClass().getName(), metricInstance.getClass().getName());
      return null;
    } else {
      logger.debug("Register name {} as metric {} successfully", name,
          metricInstance.getClass().getName());
      return new PyMetricImpl(metric);
    }
  }

  public PyMetric register(String name, Class<? extends Metric> clazz) {
    return register(name, clazz, enableMetric);
  }

  public PyMetric register(String name, Class<? extends Metric> clazz, boolean enableMetric) {
    if (!enableMetric) {
      return PyNullMetric.defaultNullMetric;
    }

    Metric metric = null;
    try {
      if (clazz.isAssignableFrom(Histogram.class)) {
        metric = super.histogram(name);
      } else {
        Metric instance;
        if (Timer.class.equals(clazz)) {
          instance = new Timer(reservoirSupplier.get());
        } else if (Histogram.class.equals(clazz)) {
          instance = new Histogram(reservoirSupplier.get());
        } else {
          instance = clazz.newInstance();
        }
        metric = super.register(name, instance);
      }
    } catch (IllegalArgumentException e) {
      metric = getMetricRegistry().getMetrics().get(name);
    } catch (Exception e) {
      logger
          .error("Can't register name {} as metric {} due to exception", name, clazz.getName(), e);
      return null;
    }
    if (metric == null) {
      logger.error("Can't register a valid metric for use");
      return null;
    }

    if (!metric.getClass().equals(clazz)) {
      logger.error("Cannot register the used name {} whose type is {} as another type {} of metric",
          name,
          metric.getClass().getName(), clazz.getName());
      return null;
    } else {
      logger.debug("Register name {} as metric {} successfully", name, clazz.getName());
      return new PyMetricImpl(metric);
    }

  }

  public boolean isEnableMetric() {
    return enableMetric;
  }

  public PyMetricRegistry setEnableMetric(boolean enableMetric) {
    this.enableMetric = enableMetric;
    return this;
  }

  public void addReporter(Closeable reporter) {
    this.reporterList.add(reporter);
  }

  public void stopReporter() {
    for (Closeable reporter : reporterList) {
      try {
        reporter.close();
      } catch (Exception e) {
        logger.error("Caught an exception when stop reporter {}", reporter.getClass().getName(), e);
      }
    }
  }

  private static class LazyHolder {
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final AtomicReference<PyMetricRegistry> register = new AtomicReference<>();

    private static synchronized void initRegister(Supplier<Reservoir> reservoirSupplier) {
      if (!initialized.get()) {
        register.set(new PyMetricRegistry(reservoirSupplier));
        initialized.compareAndSet(false, true);
      }
    }

    private static PyMetricRegistry getRegister() {
      if (!initialized.get()) {
        initRegister(ExponentiallyDecayingReservoir::new);
      }
      return register.get();
    }
  }

  public static class PyMetricNameBuilder {
    public static final String EMPTY_ID = "0";
    private final String originalName;
    private ResourceType type;
    private String id;

    public PyMetricNameBuilder(String originalName) {
      this.originalName = originalName;
    }

    public PyMetricNameBuilder type(ResourceType resourceType) {
      this.type = resourceType;
      return this;
    }

    public PyMetricNameBuilder id(String id) {
      this.id = id;
      return this;
    }

    public String build() throws Exception {
      type = (type == null) ? ResourceType.NONE : type;

      id = (id == null) ? EMPTY_ID : id;

      PyMetricNameHelper<String> pyMetricNameHelper = new PyMetricNameHelper<String>(originalName,
          type, id);
      return pyMetricNameHelper.generate();

    }

  }
}
