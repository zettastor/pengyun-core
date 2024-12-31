
package py.exception;

public class TransportException extends Exception {
  private static final long serialVersionUID = -1743063285853631752L;

  public TransportException() {
    super();
  }

  public TransportException(String message) {
    super(message);
  }

  public TransportException(String message, Throwable cause) {
    super(message, cause);
  }

  public TransportException(Throwable cause) {
    super(cause);
  }
}
