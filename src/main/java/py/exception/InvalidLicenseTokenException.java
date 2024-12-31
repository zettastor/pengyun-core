
package py.exception;

public class InvalidLicenseTokenException extends Exception {
  private static final long serialVersionUID = 1L;

  public InvalidLicenseTokenException() {
    super();
  }

  public InvalidLicenseTokenException(String message) {
    super(message);
  }

  public InvalidLicenseTokenException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidLicenseTokenException(Throwable cause) {
    super(cause);
  }
}
