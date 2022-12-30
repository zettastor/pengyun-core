/*
 * Copyright (c) 2022-2022. PengYunNetWork
 *
 * This program is free software: you can use, redistribute, and/or modify it
 * under the terms of the GNU Affero General Public License, version 3 or later ("AGPL"),
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  You should have received a copy of the GNU Affero General Public License along with
 *  this program. If not, see <http://www.gnu.org/licenses/>.
 */

package py.netty.core.twothreads;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.PlatformDependent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Consumer;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.NamedThreadFactory;
import py.consumer.ConsumerService;

public abstract class NetworkTaskEngine<E> implements ConsumerService<E> {
  static final Logger logger = LoggerFactory.getLogger(NetworkTaskEngine.class);

  private static final long START_TIME = System.nanoTime();
  private static final int ST_NOT_STARTED = 1;
  private static final int ST_STARTED = 2;
  private static final int ST_SHUTTING_DOWN = 3;
  private static final int ST_TERMINATED = 5;
  private static final AtomicIntegerFieldUpdater<NetworkTaskEngine> STATE_UPDATER
      = AtomicIntegerFieldUpdater.newUpdater(NetworkTaskEngine.class, "state");
  private final String engineName;
  private final ThreadFactory threadFactory;

  private final Consumer<? super E> consumer;
  private final Semaphore threadLock = new Semaphore(0);
  private final Promise<?> terminationFuture = new DefaultPromise<Void>(
      GlobalEventExecutor.INSTANCE);

  private List<E> tasks = new ArrayList<>();

  private Thread currentThread;
  private boolean interrupted;
  private long lastExecutionTime;

  @SuppressWarnings({"FieldMayBeFinal", "unused"})
  private volatile int state = ST_NOT_STARTED;
  private volatile long gracefulShutdownQuietPeriod;
  private volatile long gracefulShutdownTimeout;
  private long gracefulShutdownStartTime;

  public NetworkTaskEngine(Consumer<E> consumer, String engineName) {
    this.engineName = engineName;
    this.consumer = consumer;
    this.threadFactory = new NamedThreadFactory(engineName);
  }

  static long nanoTime() {
    return System.nanoTime() - START_TIME;
  }

  protected static void reject() {
    throw new RejectedExecutionException("event executor terminated");
  }

  public void interruptThread() {
    Thread currentThread = this.currentThread;
    if (currentThread == null) {
      interrupted = true;
    } else {
      currentThread.interrupt();
    }
  }

  private void addTask(E task) {
    if (task == null) {
      throw new NullPointerException("task");
    }
    if (!offerTask(task)) {
      reject();
    }
  }

  private final boolean offerTask(E task) {
    if (isTerminated()) {
      reject();
    }
    return enqueue(task);
  }

  private boolean runAllTasks() {
    int normalTasks = 0;
    int shutdownTasks = 0;
    int transferredElements = 0;
    tasks.clear();

    try {
      try {
        E element = takeElement();
        Validate.notNull(element);
        tasks.add(element);

        transferredElements = drainElements(tasks);
      } catch (Exception e) {
        logger.warn("fail to take elements from the task queue");
      }

      for (E task : tasks) {
        if (isShutdownTask(task)) {
          shutdownTasks++;
          logger.warn("exiting {} engine", engineName);
        } else {
          normalTasks++;
          try {
            safeExecute(task);
          } catch (Exception e) {
            logger.warn("fail to execute {} task", task);
          }
        }
      }
    } finally {
      afterRunningAllTasks(normalTasks);
    }

    logger.debug("{} tasks have been processed. {} shutdown tasks", normalTasks, shutdownTasks);
    Validate.isTrue(normalTasks + shutdownTasks == transferredElements + 1);
    return normalTasks > 0;
  }

  public void afterRunningAllTasks(int runTasks) {
  }

  protected void safeExecute(E task) {
    try {
      consumer.accept(task);
    } catch (Throwable t) {
      logger.warn("A task raised an exception. Task: {}", task, t);
    }
  }

  public boolean inEventLoop(Thread thread) {
    return thread == this.currentThread;
  }

  private boolean inEventLoop() {
    return inEventLoop(Thread.currentThread());
  }

  @Override
  public void start() {
    startThread();
  }

  public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
    if (quietPeriod < 0) {
      throw new IllegalArgumentException("quietPeriod: " + quietPeriod + " (expected >= 0)");
    }
    if (timeout < quietPeriod) {
      throw new IllegalArgumentException(
          "timeout: " + timeout + " (expected >= quietPeriod (" + quietPeriod + "))");
    }
    if (unit == null) {
      throw new NullPointerException("unit");
    }

    logger.debug("in shutdown");
    if (isShuttingDown()) {
      return terminationFuture();
    }

    boolean inEventLoop = inEventLoop();
    boolean commandToExit;
    int oldState;
    logger.debug("to for loop");
    for (; ; ) {
      logger.debug("in the shutdown loop");
      if (isShuttingDown()) {
        return terminationFuture();
      }
      int newState;
      commandToExit = true;
      oldState = state;
      if (inEventLoop) {
        newState = ST_SHUTTING_DOWN;
      } else {
        switch (oldState) {
          case ST_NOT_STARTED:
          case ST_STARTED:
            newState = ST_SHUTTING_DOWN;
            break;
          default:
            newState = oldState;
            commandToExit = false;
        }
      }
      if (STATE_UPDATER.compareAndSet(this, oldState, newState)) {
        break;
      }
    }
    gracefulShutdownQuietPeriod = unit.toNanos(quietPeriod);
    gracefulShutdownTimeout = unit.toNanos(timeout);

    if (oldState == ST_NOT_STARTED) {
      try {
        doStartThread();
      } catch (Throwable cause) {
        STATE_UPDATER.set(this, ST_TERMINATED);
        terminationFuture.tryFailure(cause);

        if (!(cause instanceof Exception)) {
          PlatformDependent.throwException(cause);
        }
        return terminationFuture;
      }
    }

    if (commandToExit) {
      logger.debug("exiting");
      exit(inEventLoop);
    }

    return terminationFuture();
  }

  private void exit(boolean inEventLoop) {
    if (!inEventLoop || state == ST_SHUTTING_DOWN) {
      enqueueShutdownTask();
    }
  }

  public Future<?> terminationFuture() {
    return terminationFuture;
  }

  public boolean isShuttingDown() {
    return state >= ST_SHUTTING_DOWN;
  }

  public boolean isTerminated() {
    return state == ST_TERMINATED;
  }

  protected boolean confirmShutdown() {
    if (!isShuttingDown()) {
      return false;
    }

    if (!inEventLoop()) {
      throw new IllegalStateException("must be invoked from an event loop");
    }

    if (gracefulShutdownStartTime == 0) {
      gracefulShutdownStartTime = nanoTime();
    }

    exit(true);
    if (runAllTasks()) {
      if (isTerminated()) {
        return true;
      }

      if (gracefulShutdownQuietPeriod == 0) {
        return true;
      }

      logger.debug("We still processed some tasks. We can't terminate the engine yet");
      return false;
    }

    final long nanoTime = nanoTime();

    if (isTerminated() || nanoTime - gracefulShutdownStartTime > gracefulShutdownTimeout) {
      return true;
    }

    if (nanoTime - lastExecutionTime <= gracefulShutdownQuietPeriod) {
      logger.debug("We need to wait for a quiet period. wait {} ms",
          gracefulShutdownQuietPeriod / (1000 * 1000));
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        //
      }

      return false;
    }

    return true;
  }

  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    if (unit == null) {
      throw new NullPointerException("unit");
    }

    if (inEventLoop()) {
      throw new IllegalStateException("cannot await termination of the current thread");
    }

    if (threadLock.tryAcquire(timeout, unit)) {
      threadLock.release();
    }

    return isTerminated();
  }

  @Override
  public boolean submit(E task) {
    if (isTerminated()) {
      reject();
    }

    boolean inEventLoop = inEventLoop();
    if (inEventLoop) {
      return enqueue(task);
    } else {
      startThread();
      return enqueue(task);
    }
  }

  @Override
  public void stop() {
    shutdownGracefully(1, 1, TimeUnit.SECONDS);
  }

  private void startThread() {
    if (state == ST_NOT_STARTED) {
      if (STATE_UPDATER.compareAndSet(this, ST_NOT_STARTED, ST_STARTED)) {
        try {
          doStartThread();
        } catch (Throwable cause) {
          STATE_UPDATER.set(this, ST_NOT_STARTED);
          PlatformDependent.throwException(cause);
        }
      }
    }
  }

  protected void updateLastExecutionTime() {
    lastExecutionTime = nanoTime();
  }

  private void doStartThread() {
    assert currentThread == null;
    currentThread = threadFactory.newThread(new Runnable() {
      @Override
      public void run() {
        if (interrupted) {
          currentThread.interrupt();
        }

        boolean success = false;
        updateLastExecutionTime();
        try {
          infinitelyExecuteJobs();
          success = true;
        } catch (Throwable t) {
          logger.warn("Unexpected exception from an event executor: ", t);
          logger.warn("Event loop {} is going to shutdown", this);
        } finally {
          for (; ; ) {
            int oldState = state;
            if (oldState >= ST_SHUTTING_DOWN || STATE_UPDATER
                .compareAndSet(NetworkTaskEngine.this, oldState, ST_SHUTTING_DOWN)) {
              break;
            }
          }

          if (success && gracefulShutdownStartTime == 0) {
            logger.error("Buggy " + EventExecutor.class.getSimpleName() + " implementation; "
                + NetworkTaskEngine.class.getSimpleName() + ".confirmShutdown() must be called "
                + "before run() implementation terminates.");
          }

          try {
            for (; ; ) {
              if (confirmShutdown()) {
                logger.warn("confirming shutdown.");
                break;
              }
            }
          } finally {
            try {
              cleanup();
            } finally {
              STATE_UPDATER.set(NetworkTaskEngine.this, ST_TERMINATED);
              threadLock.release();
              int size = size();
              if (size > 0) {
                logger.warn(
                    "An event executor terminated with " + "non-empty task queue (" + size + ')');
              }

              terminationFuture.setSuccess(null);
              logger.warn("task engine has been shutdown.");
            }
          }
        }
      }
    });
    currentThread.setName(engineName);
    logger.debug("ready to start {} engine", engineName);
    currentThread.start();
  }

  private void infinitelyExecuteJobs() {
    boolean ranAtLeaseOnce;
    while (true) {
      ranAtLeaseOnce = runAllTasks();
      if (ranAtLeaseOnce) {
        updateLastExecutionTime();
      }

      try {
        if (isShuttingDown()) {
          confirmShutdown();
          break;
        }
      } catch (Throwable t) {
        handleLoopException(t);
      }
    }
  }

  private void handleLoopException(Throwable t) {
    logger.warn("Unexpected exception in the selector loop.", t);

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      //
    }
  }

  protected void cleanup() {
  }

  protected abstract int size();

  protected abstract boolean enqueue(E element);

  protected abstract void enqueueShutdownTask();

  protected abstract boolean isShutdownTask(E e);

  protected abstract int drainElements(Collection<E> container);

  protected abstract E takeElement() throws Exception;
}

