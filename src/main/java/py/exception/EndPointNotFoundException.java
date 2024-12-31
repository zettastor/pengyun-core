
package py.exception;

public class EndPointNotFoundException extends Exception {
  private static final long serialVersionUID = 1L;

  public EndPointNotFoundException() {
    super();
  }

  public EndPointNotFoundException(String message) {
    super(message);
  }

  public EndPointNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public EndPointNotFoundException(Throwable cause) {
    super(cause);
  }
}
