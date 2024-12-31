
package py.engine;

import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskProcessor implements Callable<Result> {
  private static final Logger logger = LoggerFactory.getLogger(TaskProcessor.class);
  private Task task;

  TaskProcessor(Task task) {
    this.task = task;
  }

  @Override
  public Result call() throws Exception {
    try {
      this.task.doWork();
    } catch (Throwable t) {
      logger.error("caught an exception", t);
    }
    return null;
  }
}
