
package py.dd.client.exception;

public class FailedToDeactivateServiceException extends Exception {
  public FailedToDeactivateServiceException() {
    super();
  }

  public FailedToDeactivateServiceException(String message) {
    super(message);
  }

  public FailedToDeactivateServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public FailedToDeactivateServiceException(Throwable cause) {
    super(cause);
  }

}
