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
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.token.controller.TokenController;

public abstract class AbstractTaskEngine implements TaskEngine {
  private static final Logger logger = LoggerFactory.getLogger(AbstractTaskEngine.class);

  protected TokenController tokenController;
  protected int maxDrainTo = Integer.MAX_VALUE;
  protected String prefix = "default";
  protected boolean daemon = false;

  protected BlockingQueue<Task> queue;
  private Thread thread;

  @Override
  public void start() {
    if (queue == null) {
      throw new IllegalArgumentException("queue is not initialized yet");
    }

    String threadName = prefix + "-task-engine";

    thread = new Thread(threadName) {
      public void run() {
        try {
          executeJobs();
        } catch (Throwable t) {
          logger.warn("caught an exception", t);
        }
      }
    };

    thread.setDaemon(daemon);
    logger.info("The created thread {} is {} a daemon thread", threadName,
        thread.isDaemon() ? "" : "not");
    thread.start();
  }

  private void executeJobs() {
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

        if (tokenController != null) {
          int token = tokenController.tryAcquireToken(task.getToken());
          if (token == 0) {
            logger.warn("fail to control {} engine, just going on, expect token: {}", prefix,
                task.getToken());
          }
          task.setToken(token);
        }

        try {
          task.doWork();
        } catch (Throwable t) {
          logger.warn("caught an exception", t);
        }
      }
    }
  }

  @Override
  public void stop() {
    if (thread == null) {
      return;
    }

    try {
      queue.put(new StopTask());
      thread.join();
    } catch (Throwable t) {
      logger.warn("caught an exception when stopping the task engine", t);
    }
  }

  @Override
  public boolean drive(Task task) {
    try {
      queue.put(task);
      return true;
    } catch (InterruptedException e) {
      logger.error("offer was blocked and now interrupted", e);
      return false;
    } catch (Throwable t) {
      logger.error("caught an exception when putting a task to the task queue", t);
      return false;
    }

  }

  @Override
  public boolean drive(Task task, int timeout, TimeUnit timeUnit) {
    try {
      return queue.offer(task, timeout, timeUnit);
    } catch (InterruptedException e) {
      logger.error("offer was blocked and now interrupted", e);
      return false;
    } catch (Throwable t) {
      logger.error("caught an exception when putting a task to the task queue", t);
      return false;
    }
  }

  @Override
  public int getPendingTask() {
    return queue.size();
  }

  public boolean isDaemon() {
    return daemon;
  }

  public void setDaemon(boolean daemon) {
    this.daemon = daemon;
  }

  public int getMaxDrainTo() {
    return maxDrainTo;
  }

  public void setMaxDrainTo(int maxDrainTo) {
    this.maxDrainTo = maxDrainTo;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public TokenController getTokenController() {
    return tokenController;
  }

  public void setTokenController(TokenController tokenController) {
    this.tokenController = tokenController;
  }
}
