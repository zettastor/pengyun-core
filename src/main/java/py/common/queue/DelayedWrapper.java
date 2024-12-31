
package py.common.queue;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class DelayedWrapper<E> implements Delayed {
  private final E wrappedInstance;
  private long expirationTime;

  public DelayedWrapper(E wrappedInstance, long delayMs) {
    this.wrappedInstance = wrappedInstance;
    updateDelay(delayMs);
  }

  public E getWrappedInstance() {
    return wrappedInstance;
  }

  public void updateDelay(long delayMs) {
    this.expirationTime = System.currentTimeMillis() + delayMs;
  }

  @Override
  public long getDelay(@Nonnull TimeUnit unit) {
    long delayMs = expirationTime - System.currentTimeMillis();
    return unit.convert(delayMs, TimeUnit.MILLISECONDS);
  }

  @Override
  public int compareTo(@Nonnull Delayed o) {
    return Long.compare(getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
  }
}
