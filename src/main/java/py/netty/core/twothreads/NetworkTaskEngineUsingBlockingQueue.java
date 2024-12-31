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

