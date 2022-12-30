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

import static org.junit.Assert.assertEquals;
import static py.consumer.ConsumerServiceDispatcherTest.NumType.EVEN;
import static py.consumer.ConsumerServiceDispatcherTest.NumType.ODD;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.Test;
import py.test.TestBase;

public class ConsumerServiceDispatcherTest extends TestBase {
  @Test
  public void basicTest() throws Exception {
    Set<Integer> completeSetOdd = Sets.newConcurrentHashSet();
    Set<Integer> completeSetEven = Sets.newConcurrentHashSet();
    ConsumerService<Integer> serviceDispatcher = new ConsumerServiceDispatcher<>(
        integer -> integer % 2 == 0 ? EVEN : ODD,
        numType -> new DirectConsumerService<>(integer -> {
          if (numType == ODD) {
            completeSetOdd.add(integer);
          } else if (numType == EVEN) {
            completeSetEven.add(integer);
          }
        }));

    assertEquals(0, DirectConsumerService.startedCount.get());

    for (int i = 0; i < 100; i++) {
      serviceDispatcher.submit(i);
    }

    assertEquals(2, DirectConsumerService.startedCount.get());

    assertEquals(100 / 2, completeSetEven.size());
    assertEquals(100 / 2, completeSetOdd.size());

    for (Integer integer : completeSetEven) {
      assertEquals(0, integer % 2);
    }

    for (Integer integer : completeSetOdd) {
      assertEquals(1, integer % 2);
    }

    serviceDispatcher.stop();
    assertEquals(0, DirectConsumerService.startedCount.get());
  }

  enum NumType {
    ODD, EVEN
  }

  static class DirectConsumerService<E> implements ConsumerService<E> {
    static AtomicInteger startedCount = new AtomicInteger(0);
    private final Consumer<E> consumer;
    private AtomicBoolean started = new AtomicBoolean(false);

    DirectConsumerService(Consumer<E> consumer) {
      this.consumer = consumer;
    }

    @Override
    public void start() {
      if (started.compareAndSet(false, true)) {
        startedCount.incrementAndGet();
      }
    }

    @Override
    public void stop() {
      if (started.compareAndSet(true, false)) {
        startedCount.decrementAndGet();
      }
    }

    @Override
    public boolean submit(E element) {
      if (!started.get()) {
        return false;
      }
      consumer.accept(element);
      return true;
    }
  }

}