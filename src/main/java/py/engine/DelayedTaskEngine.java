
package py.engine;

import java.util.concurrent.DelayQueue;

public class DelayedTaskEngine extends AbstractTaskEngine {
  public DelayedTaskEngine() {
    queue = new DelayQueue<>();
    this.prefix = "delay";
  }
}
