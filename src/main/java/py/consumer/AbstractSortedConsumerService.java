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
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSortedConsumerService<E> implements ConsumerService<E> {
  private static final Logger logger = LoggerFactory.getLogger(AbstractSortedConsumerService.class);

  protected final String name;

  private volatile boolean shutdown = false;

  private volatile ConcurrentSkipListSet<E> idleSet;
  private AtomicReference<ConcurrentSkipListSet<E>> atomicReference;
  private List<E> elements;
  private Comparator<E> comparator;
  private Puller puller;
  private Semaphore semaphore;

  public AbstractSortedConsumerService(Comparator<E> comparator, String name) {
    this.atomicReference = new AtomicReference<>();
    this.idleSet = new ConcurrentSkipListSet<>();
    Validate.isTrue(this.atomicReference.getAndSet(new ConcurrentSkipListSet<E>()) == null);
    this.semaphore = new Semaphore(0);
    this.elements = new LinkedList<E>();
    this.comparator = comparator;
    this.name = name;
  }

  public void start() {
    puller = new Puller(name);
    puller.start();
  }

  @Override
  public boolean submit(E e) {
    boolean success = atomicReference.get().add(e);
    if (success) {
      semaphore.release();
    } else {
      logger.debug("can not add element={}", e);
    }

    return success;
  }

  protected void shutdown() {
    shutdown = true;
  }

  protected void join() {
    try {
      puller.join();
    } catch (Exception e) {
      logger.warn("fail to stop scheduler", e);
    }
  }

  protected abstract void consume(Collection<E> elements);

  private class Puller extends Thread {
    private E firstElement = null;

    public Puller(String name) {
      super(name);
    }

    public void run() {
      while (!shutdown) {
        try {
          elements.clear();
          puller();
        } catch (Throwable t) {
          logger.error("fail to start scheduler thread", t);
        }
      }

      logger.info("exit the thread, name={}", getName());
    }

    private void puller() throws Exception {
      semaphore.acquire();
      firstElement = null;

      while ((firstElement = idleSet.pollFirst()) != null) {
        elements.add(firstElement);
      }

      idleSet = atomicReference.getAndSet(idleSet);
      while ((firstElement = idleSet.pollFirst()) != null) {
        elements.add(firstElement);
      }

      int count = elements.size();
      logger.info("pool size={} every time, semaphore={}", count, semaphore.availablePermits());
      if (count < 1) {
        throw new IllegalArgumentException("count = " + count);
      }

      elements.sort(comparator);
      try {
        consume(elements);
      } finally {
        if (count > 1) {
          semaphore.acquire(count - 1);
        }
      }
    }
  }
}
