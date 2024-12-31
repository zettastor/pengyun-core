
package py.common.log.info.carrier;

import py.common.LoggerTracer;

public class ThrowableLogInfoCarrier implements LogInfoCarrier {
  private String msg;
  private Throwable throwable;

  public ThrowableLogInfoCarrier(String msg, Throwable t) {
    this.msg = msg;
    this.throwable = t;
  }

  @Override
  public String buildLogInfo() {
    return LoggerTracer.buildString(msg, throwable);
  }

  @Override
  public void release() {
    this.msg = null;
    this.throwable = null;
  }
}
