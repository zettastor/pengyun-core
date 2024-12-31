
package py.exception;

public class ServiceIsNotAvailableException extends Exception {
  private static final long serialVersionUID = 1L;

  public ServiceIsNotAvailableException() {
    super();
  }

  public ServiceIsNotAvailableException(String message) {
    super(message);
  }

  public ServiceIsNotAvailableException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServiceIsNotAvailableException(Throwable cause) {
    super(cause);
  }
}
