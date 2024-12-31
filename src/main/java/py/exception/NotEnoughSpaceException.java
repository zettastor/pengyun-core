
package py.exception;

public class NotEnoughSpaceException extends Exception {
  private static final long serialVersionUID = -8423209118838207622L;

  public NotEnoughSpaceException() {
    super();
  }

  public NotEnoughSpaceException(String message) {
    super(message);
  }

  public NotEnoughSpaceException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotEnoughSpaceException(Throwable cause) {
    super(cause);
  }
}
