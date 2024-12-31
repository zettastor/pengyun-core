

package py.dd.client.exception;

public class FailedToActivateServiceException extends Exception {
  public FailedToActivateServiceException() {
    super();
  }

  public FailedToActivateServiceException(String message) {
    super(message);
  }

  public FailedToActivateServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public FailedToActivateServiceException(Throwable cause) {
    super(cause);
  }

}
