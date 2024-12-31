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

package py.storage.impl;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DummyDeadlineSelector<T> implements DeadlineTaskSelector<T> {
  private final LinkedBlockingQueue<T> taskQueue = new LinkedBlockingQueue<>();

  @Override
  public void offer(T task, long deadlineDelayMs) {
    taskQueue.offer(task);
  }

  @Override
  public void offer(T task) {
    taskQueue.offer(task);
  }

  @Override
  public void stop() {
  }

  @Override
  public void start() {
  }

  @Override
  public T select() {
    return taskQueue.poll();
  }

  @Override
  public T select(int time, TimeUnit timeUnit) throws InterruptedException {
    return taskQueue.poll(time, timeUnit);
  }

  @Override
  public int size() {
    return taskQueue.size();
  }
}
