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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PyMetricImpl implements PyMetric {
  private static final Logger logger = LoggerFactory.getLogger(PyTimerContextImpl.class);
  private final Metric metric;

  public PyMetricImpl(Metric metric) {
    this.metric = metric;
  }

  @Override
  public void incCounter() {
    if (metric instanceof Counter) {
      ((Counter) metric).inc();
    } else {
      throw new RuntimeException("metric is not counter type");
    }
  }

  @Override
  public void incCounter(long n) {
    if (metric instanceof Counter) {
      ((Counter) metric).inc(n);
    } else {
      throw new RuntimeException("metric is not counter type");
    }
  }

  @Override
  public void decCounter() {
    if (metric instanceof Counter) {
      ((Counter) metric).dec();
    } else {
      throw new RuntimeException("metric is not counter type");
    }
  }

  @Override
  public void decCounter(long n) {
    if (metric instanceof Counter) {
      ((Counter) metric).dec(n);
    } else {
      throw new RuntimeException("metric is not counter type");
    }
  }

  @Override
  public void updateHistogram(int value) {
    if (metric instanceof Histogram) {
      ((Histogram) metric).update(value);
    } else {
      throw new RuntimeException("metric is not histogram type");
    }
  }

  @Override
  public void updateHistogram(long value) {
    if (metric instanceof Histogram) {
      ((Histogram) metric).update(value);
    } else {
      throw new RuntimeException("metric is not histogram type");
    }
  }

  @Override
  public void mark() {
    if (metric instanceof Meter) {
      ((Meter) metric).mark();
    } else {
      throw new RuntimeException("metric is not meter type");
    }
  }

  @Override
  public void mark(long n) {
    if (metric instanceof Meter) {
      ((Meter) metric).mark(n);
    } else {
      throw new RuntimeException("metric is not meter type");
    }
  }

  @Override
  public long getCount() {
    if (metric instanceof Meter) {
      return ((Meter) metric).getCount();
    } else if (metric instanceof Counter) {
      return ((Counter) metric).getCount();
    } else {
      throw new RuntimeException("metric is not meter type");
    }
  }

  @Override
  public PyTimerContext time() {
    if (metric instanceof Timer) {
      Context context = ((Timer) metric).time();
      if (context == null) {
        logger.error("got a null context !! {}", metric, metric.getClass().toString());
      }
      return new PyTimerContextImpl(context);
    } else {
      throw new RuntimeException("metric is not timer type");
    }
  }

  @Override
  public <T> T time(Callable<T> event) throws Exception {
    if (metric instanceof Timer) {
      return ((Timer) metric).time(event);
    } else {
      throw new RuntimeException("metric is not timer type");
    }
  }

  ;

  @Override
  public void update(long duration, TimeUnit unit) {
    if (metric instanceof Timer) {
      ((Timer) metric).update(duration, unit);
    } else {
      throw new RuntimeException("metric is not timer type");
    }
  }
}
