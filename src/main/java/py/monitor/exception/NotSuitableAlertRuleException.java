

package py.monitor.exception;

public class NotSuitableAlertRuleException extends Exception {
  private static final long serialVersionUID = 1L;

  public NotSuitableAlertRuleException() {
    super();
  }

  public NotSuitableAlertRuleException(String message) {
    super(message);
  }

  public NotSuitableAlertRuleException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotSuitableAlertRuleException(Throwable cause) {
    super(cause);
  }
}
