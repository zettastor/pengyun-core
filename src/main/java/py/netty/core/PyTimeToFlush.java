

package py.netty.core;

public interface PyTimeToFlush {
  public void call();

  public void incRequestCount();
}
