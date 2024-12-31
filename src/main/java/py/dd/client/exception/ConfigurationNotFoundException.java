
package py.dd.client.exception;

public class ConfigurationNotFoundException extends Exception {
  private static final long serialVersionUID = 1L;

  public ConfigurationNotFoundException() {
    super();
  }

  public ConfigurationNotFoundException(String message) {
    super(message);
  }

  public ConfigurationNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConfigurationNotFoundException(Throwable cause) {
    super(cause);
  }
}
