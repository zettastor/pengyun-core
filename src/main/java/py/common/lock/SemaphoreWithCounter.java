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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import py.common.Counter;

public class SemaphoreWithCounter extends Semaphore {
  private final Counter counter;

  public SemaphoreWithCounter(int permits, Counter counter) {
    super(permits);

    counter.increment(permits);
    this.counter = counter;
  }

  public SemaphoreWithCounter(int permits, boolean fair, Counter counter) {
    super(permits, fair);

    counter.increment(permits);
    this.counter = counter;
  }

  @Override
  public void acquire() throws InterruptedException {
    acquire(1);
  }

  @Override
  public void acquire(int permits) throws InterruptedException {
    super.acquire(permits);
    counter.decrement(permits);
  }

  @Override
  public void acquireUninterruptibly() {
    acquireUninterruptibly(1);
  }

  @Override
  public void acquireUninterruptibly(int permits) {
    super.acquireUninterruptibly(permits);
    counter.decrement(permits);
  }

  @Override
  public boolean tryAcquire() {
    return tryAcquire(1);
  }

  @Override
  public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
    return tryAcquire(1, timeout, unit);
  }

  @Override
  public boolean tryAcquire(int permits) {
    if (super.tryAcquire(permits)) {
      counter.decrement(permits);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean tryAcquire(int permits, long timeout, TimeUnit unit) throws InterruptedException {
    if (super.tryAcquire(permits, timeout, unit)) {
      counter.decrement(permits);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void release() {
    release(1);
  }

  @Override
  public void release(int permits) {
    super.release(permits);
    counter.increment(permits);
  }

  @Override
  public int drainPermits() {
    int val = super.drainPermits();
    counter.decrement(val);
    return val;
  }

  @Override
  protected void reducePermits(int reduction) {
    throw new UnsupportedOperationException();
  }

}
