
package py.exception;

public class TooManyEndPointFoundException extends Exception {
  private static final long serialVersionUID = 1L;

  public TooManyEndPointFoundException() {
    super();
  }

  public TooManyEndPointFoundException(String message) {
    super(message);
  }

  public TooManyEndPointFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public TooManyEndPointFoundException(Throwable cause) {
    super(cause);
  }
}
