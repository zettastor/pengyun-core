/**
* Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/ 

package py.periodic.impl;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import py.common.NamedThreadFactory;
import py.periodic.PeriodicWorkExecutor;
import py.periodic.UnableToStartException;
import py.periodic.Worker;
import py.periodic.WorkerFactory;

public class PeriodicWorkExecutorImpl implements PeriodicWorkExecutor {
  private static final Logger logger = Logger.getLogger(PeriodicWorkExecutorImpl.class);

  private WorkerFactory workerFactory;
  private ExecutionOptionsReader executionOptionsReader;
  private ScheduledThreadPoolExecutor executor;

  private ThreadFactory periodicThreadFactory;

  private UncaughtExceptionHandler uncaughtExceptionHandler = new UncaughtExceptionHandler() {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
      logger.error("Failure in thread " + t.getName(), e);
    }
  };

  public PeriodicWorkExecutorImpl(ExecutionOptionsReader optionReader, WorkerFactory factory) {
    this(optionReader, factory, "Periodic Worker");
  }

  public PeriodicWorkExecutorImpl(ExecutionOptionsReader optionReader, WorkerFactory factory,
      String workerThreadName) {
    workerFactory = factory;
    executionOptionsReader = optionReader;

    periodicThreadFactory = new NamedThreadFactory(workerThreadName) {
      @Override
      public Thread newThread(Runnable r) {
        Thread t = super.newThread(r);
        t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        return t;
      }
    };
  }

  public PeriodicWorkExecutorImpl() {
    this(null, null);
  }

  @Override
  public void setWorkerFactory(WorkerFactory workerFactory) {
    this.workerFactory = workerFactory;
  }

  public void setExecutionOptionsReader(ExecutionOptionsReader reader) {
    this.executionOptionsReader = reader;
  }

  @Override
  public void start() throws UnableToStartException {
    startWithDelay(0L);
  }

  @Override
  public void startWithDelay(long delayTimeMs) throws UnableToStartException {
    logger.trace("Periodic Work is kicked off");
    if (workerFactory == null || executionOptionsReader == null) {
      String errMsg = "workerFactory and executionOptionReader must be set before start workers";
      logger.error(errMsg);
      throw new UnableToStartException(errMsg);
    }

    ExecutionOptions executionOptions;
    try {
      executionOptions = executionOptionsReader.read();
    } catch (InvalidExecutionOptionsException e) {
      logger.error(e.getMessage());
      throw new UnableToStartException(e.getMessage(), e);
    }

    executor = new ScheduledThreadPoolExecutor(executionOptions.getMaxNumWorkers(),
        periodicThreadFactory);
    for (int i = 0; i < executionOptions.getNumWorkers(); i++) {
      if (executionOptions.getFixedDelay() != null) {
        executor.scheduleWithFixedDelay(new WorkerTask(workerFactory.createWorker()),
            delayTimeMs,
            executionOptions.getFixedDelay(), TimeUnit.MILLISECONDS);
      } else {
        executor.scheduleAtFixedRate(new WorkerTask(workerFactory.createWorker()),
            delayTimeMs,
            executionOptions.getFixedRate(), TimeUnit.MILLISECONDS);
      }
    }
  }

  ;

  @Override
  public void stop() {
    if (executor != null) {
      executor.shutdown();
    }
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return executor == null ? true : executor.awaitTermination(timeout, unit);
  }

  @Override
  public void stopNow() {
    if (executor != null) {
      executor.shutdownNow();
    }
  }

  public int getMaxWorkersCount() {
    return executor.getMaximumPoolSize();
  }

  public int getActiveWorkersCount() {
    return executor.getActiveCount();
  }

  private class WorkerTask implements Runnable {
    Worker worker;

    WorkerTask(Worker worker) {
      this.worker = worker;
    }

    @Override
    public void run() {
      try {
        if (executor.isShutdown()) {
          return;
        }
        worker.doWork();
      } catch (Exception e) {
        String errMsg = "An exception " + e.getClass().getName() + " thrown by the thread "
            + Thread.currentThread().getName() + " is caught. The worker will be re-submitted.";
        logger.error(errMsg, e);

      }
    }
  }
}
