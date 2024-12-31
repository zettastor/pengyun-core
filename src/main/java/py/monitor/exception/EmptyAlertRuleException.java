

package py.monitor.exception;

public class EmptyAlertRuleException extends Exception {
  private static final long serialVersionUID = 1L;

  public EmptyAlertRuleException() {
    super();
  }

  public EmptyAlertRuleException(String message) {
    super(message);
  }

  public EmptyAlertRuleException(String message, Throwable cause) {
    super(message, cause);
  }

  public EmptyAlertRuleException(Throwable cause) {
    super(cause);
  }
}
