
package py.dd.client.exception;

public class ServiceStatusIsErrorException extends Exception {
  public ServiceStatusIsErrorException() {
    super();
  }

  public ServiceStatusIsErrorException(String message) {
    super(message);
  }

  public ServiceStatusIsErrorException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServiceStatusIsErrorException(Throwable cause) {
    super(cause);
  }

}
