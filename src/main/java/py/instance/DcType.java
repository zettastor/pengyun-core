
package py.instance;

public enum DcType {
  NORMALSUPPORT(0), SCSISUPPORT(1), ALLSUPPORT(2);

  private int value;

  DcType(int value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "DCType{"
        + "value=" + value
        + '}';
  }
}
