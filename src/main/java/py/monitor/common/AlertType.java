
package py.monitor.common;

public enum AlertType {
  EQUIPMENT {
    @Override
    public String getCnName() {
      return "设备告警";
    }
  },
  CONSUMER {
    @Override
    public String getCnName() {
      return "业务告警";
    }
  };

  public abstract String getCnName();

}
