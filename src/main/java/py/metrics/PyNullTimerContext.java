
package py.metrics;

public class PyNullTimerContext implements PyTimerContext {
  public static final PyTimerContext defaultNullTimerContext = new PyNullTimerContext();

  @Override
  public long stop() {
    return -1;
  }

}
