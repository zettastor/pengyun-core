
package py.metrics;

public interface PyTimerContext extends AutoCloseable {
  long stop();

  default void close() {
    stop();
  }

}
