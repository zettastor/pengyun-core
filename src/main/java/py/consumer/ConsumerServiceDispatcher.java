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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerServiceDispatcher<T, E> implements ConsumerService<E> {
  private static final Logger logger = LoggerFactory.getLogger(ConsumerServiceDispatcher.class);

  private final ConcurrentHashMap<T, ConsumerService<E>> consumers = new ConcurrentHashMap<>();

  private final Function<E, ? extends T> typeIdentifier;

  private final Function<? super T, ConsumerService<E>> consumerServiceFactory;

  private volatile boolean isStopped;

  public ConsumerServiceDispatcher(Function<E, ? extends T> typeIdentifier,
      Function<? super T, ConsumerService<E>> consumerServiceFactory) {
    this.typeIdentifier = typeIdentifier;
    this.consumerServiceFactory = consumerServiceFactory;
  }

  @Override
  public void start() {
    isStopped = false;

  }

  @Override
  public synchronized void stop() {
    isStopped = true;
    List<T> stoppedKey = new ArrayList<>();
    for (Map.Entry<T, ConsumerService<E>> entry : consumers.entrySet()) {
      entry.getValue().stop();
      stoppedKey.add(entry.getKey());
    }

    for (T t : stoppedKey) {
      consumers.remove(t);
    }
  }

  @Override
  public boolean submit(E element) {
    if (isStopped) {
      logger.warn("consumer service has been stopped, refuse the request {}", element);
      return false;
    }

    T type = typeIdentifier.apply(element);
    if (type == null) {
      return false;
    }

    ConsumerService<E> consumer = consumers.computeIfAbsent(type, t -> {
      ConsumerService<E> newConsumer = consumerServiceFactory.apply(t);
      if (newConsumer == null) {
        return null;
      }
      newConsumer.start();
      return newConsumer;
    });

    if (consumer == null) {
      logger.warn("got a null consumer {}, the consumers' map {}", element, consumers);
      return false;
    }

    try {
      return consumer.submit(element);
    } finally {
      if (isStopped) {
        stop();
      }
    }
  }

}
