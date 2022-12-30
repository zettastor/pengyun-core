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

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractSortedSingleThreadConsumerService<E> implements ConsumerService<E> {
  private static final Logger logger = LoggerFactory
      .getLogger(AbstractSortedSingleThreadConsumerService.class);

  protected final String name;
  private final ReentrantLock lock = new ReentrantLock();
  private final Condition notEmpty = lock.newCondition();
  private final Thread worker;
  private final AtomicBoolean workingSetEmpty = new AtomicBoolean(true);
  private volatile boolean isStopped = true;
  private Consumer<Collection<E>> consumer;
  private volatile ConcurrentSkipListSet<E> idleSet;
  private volatile ConcurrentSkipListSet<E> workingSet;

  protected AbstractSortedSingleThreadConsumerService(String name, Comparator<E> comparator) {
    this.name = name;
    if (comparator != null) {
      this.idleSet = new ConcurrentSkipListSet<>(comparator);
      this.workingSet = new ConcurrentSkipListSet<>(comparator);
    } else {
      this.idleSet = new ConcurrentSkipListSet<>();
      this.workingSet = new ConcurrentSkipListSet<>();
    }
    this.worker = new Thread(this::work, name);
  }

  protected void setConsumer(Consumer<E> consumer) {
    this.consumer = es -> es.forEach(consumer);
  }

  protected void setBatchedConsumer(Consumer<Collection<E>> consumer) {
    this.consumer = consumer;
  }

  private void swapSet() {
    ConcurrentSkipListSet<E> tmp = idleSet;
    idleSet = workingSet;
    workingSet = tmp;
    workingSetEmpty.set(true);
  }

  private void work() {
    List<E> elements = new LinkedList<>();
    while (true) {
      if (!isStopped) {
        lock.lock();
        try {
          if (workingSetEmpty.get()) {
            try {
              notEmpty.await();
            } catch (InterruptedException ignore) {
              logger.error("caught exception", ignore);
            }
          }
          swapSet();
        } finally {
          lock.unlock();
        }
      }
      elements.clear();
      while (!idleSet.isEmpty()) {
        elements.add(idleSet.pollFirst());
      }
      if (!elements.isEmpty()) {
        logger.debug("got {} tasks", elements.size());
        consumer.accept(elements);
      } else {
        logger.debug("empty");
      }

      boolean isLastRound = isStopped;
      if (isLastRound) {
        break;
      }
    }
  }

  @Override
  public synchronized void start() {
    if (!isStopped) {
      logger.warn("already started, no need to start again {}", name);
    } else {
      if (consumer == null) {
        logger.error("set consumer first", new Exception());
        throw new IllegalStateException();
      }
      isStopped = false;
      worker.start();
    }
  }

  @Override
  public synchronized void stop() {
    if (isStopped) {
      logger.warn("already stopped or not started, no need to stop {}", name);
    } else {
      isStopped = true;
      signalNotEmpty();
      try {
        worker.join();
      } catch (InterruptedException e) {
        logger.warn("interrupted before stopped", e);
      }
    }
  }

  @Override
  public boolean submit(E element) {
    if (isStopped) {
      return false;
    }

    if (element == null) {
      throw new NullPointerException();
    }
    workingSet.add(element);
    if (workingSetEmpty.compareAndSet(true, false)) {
      signalNotEmpty();
    }
    return !isStopped;
  }

  private void signalNotEmpty() {
    lock.lock();
    try {
      notEmpty.signal();
    } finally {
      lock.unlock();
    }
  }

}
