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

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NetworkTaskEngineUsingBlockingQueue<E> extends NetworkTaskEngine<E> {
  static final Logger logger = LoggerFactory.getLogger(NetworkTaskEngineUsingBlockingQueue.class);
  private final BlockingQueue<E> queue;

  public NetworkTaskEngineUsingBlockingQueue(Consumer<E> consumer, String engineName) {
    super(consumer, engineName);
    queue = newTaskQueue();
  }

  protected BlockingQueue<E> newTaskQueue() {
    return new LinkedBlockingQueue<E>();
  }

  @Override
  protected int size() {
    return queue.size();
  }

  @Override
  protected boolean enqueue(E element) {
    try {
      queue.put(element);
      return true;
    } catch (Throwable t) {
      logger.warn("failed to insert an element to the task engine", t);
      return false;
    }
  }

  @Override
  protected int drainElements(Collection<E> container) {
    Validate.notNull(container, "container is null at the operation of draining");

    return queue.drainTo(container);
  }

  @Override
  protected E takeElement() {
    try {
      return queue.take();
    } catch (InterruptedException e) {
      logger.warn("interrupted", e);
      return null;
    }
  }

  protected abstract void enqueueShutdownTask();

  protected abstract boolean isShutdownTask(E e);
}

