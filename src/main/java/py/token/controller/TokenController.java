
package py.token.controller;

import java.util.concurrent.TimeUnit;

public interface TokenController {
  public long getId();

  public void reset();

  public boolean acquireToken(long timeout, TimeUnit timeUnit);

  public boolean acquireToken(int tokenCount, long timeout, TimeUnit timeUnit);

  public boolean acquireToken();

  public boolean acquireToken(int tokenCount);

  public int tryAcquireToken(int tokenCount);

  public void updateToken(int bucketCapacity);
}
