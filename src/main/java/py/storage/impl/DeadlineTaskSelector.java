
package py.storage.impl;

public interface DeadlineTaskSelector<T> extends TaskSelector<T> {
  void offer(T task, long deadlineDelayMs);

  void start();

  void stop();
}
