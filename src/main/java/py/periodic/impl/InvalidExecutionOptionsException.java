

package py.periodic.impl;

public class InvalidExecutionOptionsException extends Exception {
  private static final long serialVersionUID = 1L;

  public InvalidExecutionOptionsException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidExecutionOptionsException() {
    super();
  }

  public InvalidExecutionOptionsException(String message) {
    super(message);
  }

}
