
package py.exception;

public class LicenseAlreadyExistedException extends Exception {
  private static final long serialVersionUID = 1L;

  public LicenseAlreadyExistedException() {
    super();
  }

  public LicenseAlreadyExistedException(String message) {
    super(message);
  }

  public LicenseAlreadyExistedException(String message, Throwable cause) {
    super(message, cause);
  }

  public LicenseAlreadyExistedException(Throwable cause) {
    super(cause);
  }
}
