
package py.io.sequential;

public interface IoSequentialTypeHolder {
  long getOffset();

  int getLength();

  IoSequentialType getIoSequentialType();

  void setIoSequentialType(IoSequentialType ioSequentialType);

  public enum IoSequentialType {
    UNKNOWN(0),
    SEQUENTIAL_TYPE(1),
    RANDOM_TYPE(2),
    ;
    private int val;

    IoSequentialType(int val) {
      this.val = val;
    }

    public int val() {
      return val;
    }
  }
}
