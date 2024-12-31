
package py.engine;

public class ResultImpl implements Result {
  public static final Result DEFAULT = new ResultImpl();
  private Exception exception;

  public ResultImpl() {
    this(null);
  }

  public ResultImpl(Exception exception) {
    this.exception = exception;
  }

  @Override
  public boolean isSuccess() {
    return exception == null;
  }

  @Override
  public Exception cause() {
    return exception;
  }

  public void setCause(Exception e) {
    this.exception = e;
  }

  @Override
  public String toString() {
    return "ResultImpl [e=" + exception + "]";
  }
}
