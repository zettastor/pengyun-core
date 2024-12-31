

package py.monitor.common;

public enum DiskInfoLightStatus {
  OFF(0),
  ON(1),
  UNKNOWN(2);

  private int value;

  DiskInfoLightStatus(int value) {
    this.value = value;
  }

  public static DiskInfoLightStatus getFromValue(int value) {
    for (DiskInfoLightStatus lightStatus : DiskInfoLightStatus.values()) {
      if (lightStatus.value == value) {
        return lightStatus;
      }
    }
    return UNKNOWN;
  }

  public int getValue() {
    return value;
  }
}
