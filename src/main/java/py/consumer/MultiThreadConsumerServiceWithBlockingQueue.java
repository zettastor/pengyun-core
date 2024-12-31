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

package py.consumer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MultiThreadConsumerServiceWithBlockingQueue<E> extends
    AbstractMultiThreadConsumerService<E> {
  private final BlockingQueue<E> elementQueue;

  public MultiThreadConsumerServiceWithBlockingQueue(int threadCount, Consumer<? super E> consumer,
      BlockingQueue<E> elementQueue, String name) {
    super(threadCount, consumer, name);
    this.elementQueue = elementQueue;
  }

  @Override
  public int size() {
    return elementQueue.size();
  }

  @Override
  protected boolean enqueue(E element) {
    return elementQueue.offer(element);
  }

  @Override
  protected E pollElement() {
    return elementQueue.poll();
  }

  @Override
  protected E pollElement(int time, TimeUnit timeUnit) throws InterruptedException {
    return elementQueue.poll(time, timeUnit);
  }
}
