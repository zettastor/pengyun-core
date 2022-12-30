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

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeadlineTaskSelectorImpl<E> implements DeadlineTaskSelector<E> {
  private static final Logger logger = LoggerFactory.getLogger(DeadlineTaskSelectorImpl.class);
  private static final long tickDuration = 5;
  private final PriorityBlockingQueue<Element> sortedQueue;
  private final BlockingQueue<Element> expiredQueue;
  private final AtomicInteger size = new AtomicInteger(0);
  private final AtomicInteger wheelElement = new AtomicInteger();
  private HashedWheelTimer hashedWheelTimer = null;

  public DeadlineTaskSelectorImpl(Comparator<E> comparator) {
    this.sortedQueue = new PriorityBlockingQueue<>(11,
        (o1, o2) -> comparator.compare(o1.element, o2.element));
    this.expiredQueue = new LinkedBlockingQueue<>();
  }

  @Override
  public void offer(E e) {
    sortedQueue.offer(new Element(e));
    size.incrementAndGet();
  }

  public void offer(E e, long deadlineDelayMs) {
    Element element = new Element(e);
    sortedQueue.offer(element);

    try {
      Timeout timeout = hashedWheelTimer.newTimeout(timerTask -> {
        expiredQueue.offer(element);
        wheelElement.decrementAndGet();
      }, deadlineDelayMs, TimeUnit.MILLISECONDS);
      if (!element.setTimeout(timeout)) {
        timeout.cancel();
      }
    } catch (Exception ex) {
      logger.error("new hashed wheel timer task {} failed, maybe it has stopped", e, ex);
    }
    wheelElement.incrementAndGet();
    size.incrementAndGet();
  }

  @Override
  public synchronized void stop() {
    if (hashedWheelTimer != null) {
      Set<Timeout> set = hashedWheelTimer.stop();
      if (set != null && set.size() != 0) {
        logger.error(
            "stop hashed Wheel timer, but these found some (size {}) timeout task {} in timer ",
            set.size(), set);
      }
      hashedWheelTimer = null;
    } else {
      logger
          .warn("task selector may be has been stopped or has not start, do not anything for stop");
    }
  }

  @Override
  public synchronized void start() {
    if (hashedWheelTimer != null) {
      logger.warn("task selector has been open! nothing need to do! current hashed wheel timer {}",
          hashedWheelTimer);
      return;
    }
    hashedWheelTimer = new HashedWheelTimer(tickDuration, TimeUnit.MILLISECONDS);
  }

  public E select(int time, TimeUnit timeUnit) throws InterruptedException {
    E e = select();
    if (e != null) {
      return e;
    } else {
      Element element = sortedQueue.poll(time, timeUnit);
      if (element == null) {
        return null;
      } else {
        Optional<E> optionalE = element.outQueue();
        if (optionalE.isPresent()) {
          size.decrementAndGet();
          return optionalE.get();
        } else {
          return null;
        }
      }
    }
  }

  @Override
  public E select() {
    try {
      return internalSelect();
    } finally {
      logger.info("nothing need to do here");
    }
  }

  private E internalSelect() {
    if (sortedQueue.isEmpty() && wheelElement.get() == 0 && expiredQueue.isEmpty()) {
      return null;
    }

    cleanHead();
    cleanAll();

    while (true) {
      Element element;
      Optional<E> optionalE;

      if ((element = expiredQueue.poll()) != null) {
        optionalE = element.outQueue();
        if (optionalE.isPresent()) {
          size.decrementAndGet();
          return optionalE.get();
        }
      } else if ((element = sortedQueue.poll()) != null) {
        optionalE = element.outQueue();
        if (optionalE.isPresent()) {
          size.decrementAndGet();
          return optionalE.get();
        }
      } else {
        return null;
      }
    }
  }

  private void cleanHead() {
    while (!sortedQueue.isEmpty()) {
      Element peek = sortedQueue.peek();
      if (peek != null && peek.isOutQueued()) {
        Element poll = sortedQueue.poll();
        if (poll != null && poll != peek) {
          sortedQueue.offer(poll);
        }
      } else {
        break;
      }
    }
  }

  private void cleanAll() {
    if (sortedQueue.size() > size.get() * 2) {
      List<Element> temp = new ArrayList<>(sortedQueue.size());
      sortedQueue.drainTo(temp);
      for (Element element : temp) {
        if (!element.isOutQueued()) {
          sortedQueue.offer(element);
        }
      }
    }
  }

  @Override
  public int size() {
    return size.get();
  }

  class Element {
    final E element;
    final AtomicBoolean outQueued = new AtomicBoolean(false);
    volatile Timeout timeout;

    Element(E element) {
      this.element = element;
      timeout = null;
    }

    public Timeout getTimeout() {
      return timeout;
    }

    public boolean setTimeout(Timeout timeout) {
      this.timeout = timeout;
      return !outQueued.get();
    }

    boolean isOutQueued() {
      return outQueued.get();
    }

    Optional<E> outQueue() {
      if (outQueued.compareAndSet(false, true)) {
        if (null != timeout) {
          timeout.cancel();
        }
        return Optional.of(element);
      } else {
        return Optional.empty();
      }
    }

    @Override
    public String toString() {
      return "Element{"
          + "element=" + element
          + ", outQueued="
          + outQueued
          + ", timeout=" + timeout
          + '}';
    }
  }
}
