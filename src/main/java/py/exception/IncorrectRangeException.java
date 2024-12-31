
package py.exception;

public class IncorrectRangeException extends Exception {
  private static final long serialVersionUID = 1L;

  public IncorrectRangeException() {
    super();
  }

  public IncorrectRangeException(String message) {
    super(message);
  }

  public IncorrectRangeException(String message, Throwable cause) {
    super(message, cause);
  }

  public IncorrectRangeException(Throwable cause) {
    super(cause);
  }
}
