

package py.common;

public class OffsetAndLength {
  public static final int INVALID_OFFSET = -1;
  private final int offset;
  private final int length;

  public OffsetAndLength(int offset, int length) {
    this.offset = offset;
    this.length = length;
  }

  public boolean legal() {
    return (offset == INVALID_OFFSET) ? false : true;
  }

  public int getOffset() {
    return offset;
  }

  public int getLength() {
    return length;
  }

}
