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

package py.storage.impl.scheduling;

import java.nio.channels.CompletionHandler;
import py.storage.impl.Task;

public class TaskInfo {
  private final Task task;
  private final long generateTime;
  private final CompletionHandler<Integer, Object> storageCallback;
  private volatile long submitTime;

  public TaskInfo(Task task,
      CompletionHandler<Integer, Object> storageCallback) {
    this.task = task;
    this.storageCallback = storageCallback;
    generateTime = System.currentTimeMillis();
  }

  public void submitDone() {
    submitTime = System.currentTimeMillis();
  }

  public Task getTask() {
    return task;
  }

  public long getGenerateTime() {
    return generateTime;
  }

  public long getSubmitTime() {
    return submitTime;
  }

  public CompletionHandler<Integer, Object> getStorageCallback() {
    return storageCallback;
  }
}
