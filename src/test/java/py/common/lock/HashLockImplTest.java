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

package py.common.lock;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import org.apache.commons.lang.Validate;
import org.junit.Test;
import py.test.TestBase;

public class HashLockImplTest extends TestBase {
  @Test
  public void test() throws InterruptedException {
    final int threadCount = 100;
    final int valueCount = 10;
    final int loop = 100;
    final HashLock<Integer> lock = new HashLockImpl<>();
    final int[] values = new int[valueCount];

    for (int i = 0; i < valueCount; i++) {
      values[i] = 0;
    }

    Thread[] threads = new Thread[threadCount];

    for (int i = 0; i < threadCount; i++) {
      threads[i] = new Thread(() -> {
        try {
          for (int j = 0; j < loop; j++) {
            testLock(lock, values);
          }
        } catch (Throwable t) {
          logger.error("t", t);
        }
      }, "lock-test-" + i);
    }

    for (Thread thread : threads) {
      thread.start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    for (int value : values) {
      assertEquals(threadCount * loop, value);
    }
  }

  private void testLock(HashLock<Integer> lock, int[] values) {
    ArrayList<Integer> order = new ArrayList<>(values.length);
    for (int i = 0; i < values.length; i++) {
      order.add(i);
    }
    Collections.shuffle(order);

    for (Integer index : order) {
      try {
        lock.lock(index);
      } catch (InterruptedException ignore) {
        logger.error("caught exception", ignore);
      }

      values[index] = values[index] + 1;

      lock.unlock(index);
    }
  }

  @Test
  public void test1() {
    HashLock<Integer> hashLock = new HashLockImpl<>();
    Validate.isTrue(hashLock.tryLock(11));
  }

}