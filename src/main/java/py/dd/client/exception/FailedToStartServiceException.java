
package py.dd.client.exception;

public class FailedToStartServiceException extends Exception {
  public FailedToStartServiceException() {
    super();
  }

  public FailedToStartServiceException(String message) {
    super(message);
  }

  public FailedToStartServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public FailedToStartServiceException(Throwable cause) {
    super(cause);
  }

}
