
package py.exception;

public class DeleteLicenseFailedException extends LicenseException {
  private static final long serialVersionUID = 1L;

  public DeleteLicenseFailedException() {
    super();
  }

  public DeleteLicenseFailedException(String message) {
    super(message);
  }

  public DeleteLicenseFailedException(String message, Throwable cause) {
    super(message, cause);
  }

  public DeleteLicenseFailedException(Throwable cause) {
    super(cause);
  }
}
