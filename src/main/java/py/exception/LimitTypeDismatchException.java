

package py.exception;

public class LimitTypeDismatchException extends Exception {
  private static final long serialVersionUID = 1L;

  public LimitTypeDismatchException() {
    super();

  }

  public LimitTypeDismatchException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);

  }

  public LimitTypeDismatchException(String message, Throwable cause) {
    super(message, cause);

  }

  public LimitTypeDismatchException(String message) {
    super(message);

  }

  public LimitTypeDismatchException(Throwable cause) {
    super(cause);

  }

}
