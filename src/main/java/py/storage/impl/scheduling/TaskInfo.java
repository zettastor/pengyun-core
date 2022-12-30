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
