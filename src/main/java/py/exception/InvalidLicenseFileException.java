
package py.exception;

public class InvalidLicenseFileException extends Exception {
  private static final long serialVersionUID = 1L;

  public InvalidLicenseFileException() {
    super();
  }

  public InvalidLicenseFileException(String message) {
    super(message);
  }

  public InvalidLicenseFileException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidLicenseFileException(Throwable cause) {
    super(cause);
  }
}
