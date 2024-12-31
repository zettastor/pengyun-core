
package py.dd.client.exception;

public class DriverIsAliveException extends Exception {
  public DriverIsAliveException() {
    super();
  }

  public DriverIsAliveException(String message) {
    super(message);
  }

  public DriverIsAliveException(String message, Throwable cause) {
    super(message, cause);
  }

  public DriverIsAliveException(Throwable cause) {
    super(cause);
  }

}
