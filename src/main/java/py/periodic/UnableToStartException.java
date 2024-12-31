
package py.periodic;

public class UnableToStartException extends Exception {
  private static final long serialVersionUID = 1L;

  public UnableToStartException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnableToStartException() {
    super();
  }

  public UnableToStartException(String message) {
    super(message);
  }

}
