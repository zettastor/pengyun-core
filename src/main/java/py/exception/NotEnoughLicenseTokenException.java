

package py.exception;

public class NotEnoughLicenseTokenException extends Exception {
  private static final long serialVersionUID = 1L;

  public NotEnoughLicenseTokenException() {
    super();
  }

  public NotEnoughLicenseTokenException(String message) {
    super(message);
  }

  public NotEnoughLicenseTokenException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotEnoughLicenseTokenException(Throwable cause) {
    super(cause);
  }
}
