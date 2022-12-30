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
