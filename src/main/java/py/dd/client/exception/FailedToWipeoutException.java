

package py.dd.client.exception;

public class FailedToWipeoutException extends Exception {
  public FailedToWipeoutException() {
    super();
  }

  public FailedToWipeoutException(String message) {
    super(message);
  }

  public FailedToWipeoutException(String message, Throwable cause) {
    super(message, cause);
  }

  public FailedToWipeoutException(Throwable cause) {
    super(cause);
  }
}
