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

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.Counter;

public abstract class AbstractMultiThreadConsumerService<E> implements ConsumerService<E> {
  private static final Logger logger = LoggerFactory
      .getLogger(AbstractMultiThreadConsumerService.class);

  private final Consumer<? super E> consumer;

  private final Thread[] workers;

  private final String name;

  private final int threadCount;

  private volatile boolean isStopped = true;

  private Counter counter = Counter.NullCounter;

  protected AbstractMultiThreadConsumerService(int threadCount, Consumer<? super E> consumer,
      String name) {
    this.consumer = consumer;
    this.name = name;
    this.workers = new Thread[threadCount];
    this.threadCount = threadCount;
  }

  @Override
  public synchronized void start() {
    if (!isStopped) {
      logger.warn("already started, no need to start again {}", name);
    } else {
      isStopped = false;
      for (int i = 0; i < threadCount; i++) {
        workers[i] = new Thread(this::work, name + "-" + i);
      }
      for (int i = 0; i < threadCount; i++) {
        workers[i].start();
      }
    }
  }

  @Override
  public synchronized void stop() {
    if (isStopped) {
      logger.warn("already stopped or not started, no need to stop {}", name);
    } else {
      isStopped = true;
      try {
        for (Thread thread : workers) {
          thread.join();
        }
      } catch (InterruptedException e) {
        logger.warn("interrupted before stopped", e);
      }
    }
  }

  public void setCounter(Counter counter) {
    this.counter = counter;
  }

  private void work() {
    try {
      internalWork();
    } catch (Throwable t) {
      logger.error("caught an throwable, exit!", t);
    }
  }

  private void internalWork() throws InterruptedException {
    while (true) {
      if (!isStopped) {
        E element = pollElement(1, TimeUnit.SECONDS);
        if (element == null) {
          continue;
        }
        counter.decrement();
        consumer.accept(element);
      } else {
        logger.warn("got a stop signal, deal with the last {} elements", size());
        E element;
        while ((element = pollElement()) != null) {
          counter.decrement();
          consumer.accept(element);
        }
        break;
      }
    }
  }

  @Override
  public boolean submit(E element) {
    if (isStopped) {
      return false;
    } else if (enqueue(element)) {
      counter.increment();
      return !isStopped;
    }

    return false;
  }

  public abstract int size();

  protected abstract boolean enqueue(E element);

  protected abstract E pollElement();

  protected abstract E pollElement(int time, TimeUnit timeUnit) throws InterruptedException;

}
