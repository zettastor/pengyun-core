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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HashLockImpl<T> implements HashLock<T> {
  private final ConcurrentHashMap<T, AtomicInteger> locks = new ConcurrentHashMap<>();

  @Override
  public void lock(T val) throws InterruptedException {
    for (; ; ) {
      AtomicInteger waitingCount = locks.computeIfAbsent(val, v -> new AtomicInteger(0));
      synchronized (waitingCount) {
        if (waitingCount == locks.get(val)) {
          int waiting = waitingCount.getAndIncrement();
          if (waiting != 0) {
            waitingCount.wait();
          }
          return;
        }
      }
    }
  }

  @Override
  public boolean tryLock(T val) {
    AtomicInteger waitingCount = locks.putIfAbsent(val, new AtomicInteger(1));
    return waitingCount == null;
  }

  @Override
  public void unlock(T val) {
    AtomicInteger waitingCount = locks.get(val);
    synchronized (waitingCount) {
      int waiting = waitingCount.decrementAndGet();
      if (waiting != 0) {
        waitingCount.notify();
      } else {
        locks.remove(val);
      }
    }
  }

}
