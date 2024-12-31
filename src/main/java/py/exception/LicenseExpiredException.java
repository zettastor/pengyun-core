

package py.exception;

public class LicenseExpiredException extends Exception {
  private static final long serialVersionUID = 1L;

  public LicenseExpiredException() {
    super();
  }

  public LicenseExpiredException(String message) {
    super(message);
  }

  public LicenseExpiredException(String message, Throwable cause) {
    super(message, cause);
  }

  public LicenseExpiredException(Throwable cause) {
    super(cause);
  }
}
