
package py.periodic;

import java.util.concurrent.TimeUnit;

public interface PeriodicWorkExecutor {
  public void setWorkerFactory(WorkerFactory workerFactory);

  public void start() throws UnableToStartException;

  void startWithDelay(long delayTimeMs) throws UnableToStartException;

  public void stop();

  public void stopNow();

  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
}
