
package py.netty.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.netty.core.nio.SingleThreadEventLoop;

public class PyTimeToFlushImpl implements PyTimeToFlush {
  private static final Logger logger = LoggerFactory.getLogger(PyTimeToFlushImpl.class);

  private SingleThreadEventLoop singleThreadEventLoop;
  private int requestCount;

  public PyTimeToFlushImpl(SingleThreadEventLoop singleThreadEventLoop) {
    this.singleThreadEventLoop = singleThreadEventLoop;
  }

  @Override
  public void call() {
    logger.debug("before flush, {} message have been written", this.requestCount);
    this.requestCount = 0;
    this.singleThreadEventLoop.flushAllChannels();
  }

  @Override
  public void incRequestCount() {
    this.requestCount++;
  }
}
