
package py.storage.impl;

import java.util.concurrent.TimeUnit;

public interface TaskSelector<E> {
  void offer(E e);

  E select();

  E select(int time, TimeUnit timeUnit) throws InterruptedException;

  int size();
}
