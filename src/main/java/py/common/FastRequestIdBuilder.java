
package py.common;

import java.util.concurrent.atomic.AtomicLong;

public class FastRequestIdBuilder {
  private static final AtomicLong idGenerator = new AtomicLong();

  public static long get() {
    return idGenerator.getAndIncrement();
  }
}
