
package py.exception;

public class InternalErrorException extends Exception {
  private static final long serialVersionUID = 6807017079834488014L;

  public InternalErrorException() {
    super();
  }

  public InternalErrorException(String message) {
    super(message);
  }

  public InternalErrorException(String message, Throwable cause) {
    super(message, cause);
  }

  public InternalErrorException(Throwable cause) {
    super(cause);
  }
}
