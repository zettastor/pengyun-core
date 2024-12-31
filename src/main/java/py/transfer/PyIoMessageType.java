

package py.transfer;

public enum PyIoMessageType {
  ReadData(1), WriteData(2), CrossReadData(3), CrossWriteData(4);

  private final int value;

  private PyIoMessageType(int value) {
    this.value = value;
  }

  public static PyIoMessageType findByValue(int value) {
    switch (value) {
      case 1:
        return ReadData;
      case 2:
        return WriteData;
      case 3:
        return CrossReadData;
      case 4:
        return CrossWriteData;
      default:
        return null;
    }
  }

  public int getValue() {
    return value;
  }
}
