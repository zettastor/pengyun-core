

package py.netty.client;

import py.netty.core.MethodCallback;

public interface TimerTaskRemover<T> {
  public MethodCallback<T> removeTimer(long requestId);
}
