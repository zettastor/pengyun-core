

package py.exception;

public class LicenseNotExistedException extends Exception {
  private static final long serialVersionUID = 1L;

  public LicenseNotExistedException() {
    super();
  }

  public LicenseNotExistedException(String message) {
    super(message);
  }

  public LicenseNotExistedException(String message, Throwable cause) {
    super(message, cause);
  }

  public LicenseNotExistedException(Throwable cause) {
    super(cause);
  }
}
