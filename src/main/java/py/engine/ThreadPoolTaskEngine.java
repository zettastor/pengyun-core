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

package py.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.NamedThreadFactory;
import py.token.controller.TokenControllerUtils;

public class ThreadPoolTaskEngine extends AbstractTaskEngine {
  private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTaskEngine.class);
  private final BlockingQueue<Task> queue;
  private final ThreadPoolExecutor workerPool;
  private Thread pullerThread;

  public ThreadPoolTaskEngine(int corePoolSize, int maxPoolSize) {
    this(corePoolSize, maxPoolSize, false);
  }

  public ThreadPoolTaskEngine(int corePoolSize, int maxPoolSize, boolean allowCoreThreadTimeOut) {
    queue = new LinkedBlockingQueue<>();
    workerPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 60, TimeUnit.SECONDS,
        new SynchronousQueue<>(),
        new NamedThreadFactory("worker-pool"));
    workerPool.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
    this.tokenController = TokenControllerUtils.generateBogusController();
  }

  @Override
  public void start() {
    pullerThread = new Thread(prefix + "puller-Thread") {
      public void run() {
        try {
          pullWork();
        } catch (Throwable t) {
          logger.warn("caught an exception", t);
        }
      }
    };

    logger.info("start puller thread:{}", pullerThread.getName());
    pullerThread.start();

  }

  private void pullWork() {
    List<Task> tasks = new ArrayList<Task>();
    while (true) {
      tasks.clear();
      if (0 == queue.drainTo(tasks, maxDrainTo)) {
        try {
          tasks.add(queue.take());
        } catch (Exception e) {
          logger.warn("fail to task from queue", e);
        }
      }

      for (Task task : tasks) {
        if (task instanceof StopTask) {
          logger.warn("exit {} queue", prefix);
          return;
        }

        if (task.isCancel()) {
          logger.warn("task: {} has been cancelled", task);
          continue;
        }

        boolean needPutBack = false;
        try {
          TaskProcessor taskProcessor = new TaskProcessor(task);
          workerPool.execute(new FutureTask<>(taskProcessor));
        } catch (RejectedExecutionException re) {
          needPutBack = true;
          logger.info("Because of RejectedExecutionException, Can't submit a task to work threads");
        } catch (Exception e) {
          logger.error("submit task, caught an exception", e);
          needPutBack = true;
        } finally {
          if (needPutBack) {
            drive(task);
          }
        }
      }
    }
  }

  @Override
  public void stop() {
    try {
      queue.put(new StopTask());
      Validate.notNull(pullerThread);
      pullerThread.join();
      workerPool.shutdown();
      workerPool.awaitTermination(10, TimeUnit.SECONDS);
    } catch (Throwable t) {
      logger.warn("caught an exception", t);
    }
  }

  @Override
  public boolean drive(Task task) {
    return queue.offer(task);
  }

  @Override
  public boolean drive(Task task, int timeout, TimeUnit timeUnit) {
    try {
      return queue.offer(task, timeout, timeUnit);
    } catch (InterruptedException e) {
      logger.error("caught an exception", e);
      return false;
    }
  }

  @Override
  public int getPendingTask() {
    return queue.size();
  }
}
