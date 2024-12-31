

package py.engine;

public interface Result {
  public boolean isSuccess();

  public Exception cause();
}
