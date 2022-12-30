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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTask implements Task {
  public static final TaskListener DEFAULTLISTENER = new DefaultListener();
  private static final Logger logger = LoggerFactory.getLogger(AbstractTask.class);
  private final TaskListener taskListener;

  private volatile boolean isCancelled = false;
  private int token = 1;

  public AbstractTask() {
    this(DEFAULTLISTENER);
  }

  public AbstractTask(TaskListener resultListener) {
    this.taskListener = resultListener;
  }

  public TaskListener getTaskListener() {
    return taskListener;
  }

  public abstract Result work();

  public void doWork() {
    try {
      taskListener.response(work());
    } catch (Exception e) {
      taskListener.response(new ResultImpl(e));
    }
  }

  public void cancel() {
    logger.warn("cancel the task={}", this);
    isCancelled = true;
  }

  public boolean isCancel() {
    return isCancelled;
  }

  public int getToken() {
    return token;
  }

  public void setToken(int token) {
    this.token = token;
  }

  @Override
  public void destroy() {
  }

  @Override
  public long getDelay(TimeUnit unit) {
    throw new NotImplementedException("");
  }

  @Override
  public int compareTo(Delayed o) {
    throw new NotImplementedException("");
  }

  private static class DefaultListener implements TaskListener {
    @Override
    public void response(Result result) {
      if (result != null && result.cause() != null) {
        logger.error("default listener get a cause", result.cause());
      }
    }
  }
}
