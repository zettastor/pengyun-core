

package py.netty.core;

public enum IoEventThreadsMode {
  Fix_Threads_Mode(0),
  Calculate_From_Available_Core(1);

  int value;

  IoEventThreadsMode(int value) {
    this.value = value;
  }

  public static IoEventThreadsMode findByValue(int value) {
    switch (value) {
      case 0:
        return Fix_Threads_Mode;
      case 1:
        return Calculate_From_Available_Core;
      default:
        return null;
    }
  }

  public int getValue() {
    return value;
  }
}
