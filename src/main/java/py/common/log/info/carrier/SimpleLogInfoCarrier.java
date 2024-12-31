
package py.common.log.info.carrier;

public class SimpleLogInfoCarrier implements LogInfoCarrier {
  private String msg;

  public SimpleLogInfoCarrier(String msg) {
    this.msg = msg;
  }

  @Override
  public String buildLogInfo() {
    return this.msg;
  }

  @Override
  public void release() {
    this.msg = null;
  }
}
