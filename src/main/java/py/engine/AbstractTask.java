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
