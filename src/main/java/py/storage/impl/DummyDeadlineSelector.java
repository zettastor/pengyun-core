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
