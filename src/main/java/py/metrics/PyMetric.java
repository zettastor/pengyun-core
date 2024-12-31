

package py.metrics;

import com.codahale.metrics.Timer.Context;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public interface PyMetric {
  public void incCounter();

  public void incCounter(long n);

  public void decCounter();

  public void decCounter(long n);

  public void updateHistogram(int value);

  public void updateHistogram(long value);

  public void mark();

  public void mark(long n);

  public long getCount();

  public PyTimerContext time();

  public <T> T time(Callable<T> event) throws Exception;

  public void update(long duration, TimeUnit unit);
}
