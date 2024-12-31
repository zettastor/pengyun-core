
package py.exception;

public class GenericThriftClientFactoryException extends Exception {
  private static final long serialVersionUID = 1L;

  public GenericThriftClientFactoryException() {
    super();
  }

  public GenericThriftClientFactoryException(String message) {
    super(message);
  }

  public GenericThriftClientFactoryException(String message, Throwable cause) {
    super(message, cause);
  }

  public GenericThriftClientFactoryException(Throwable cause) {
    super(cause);
  }
}
