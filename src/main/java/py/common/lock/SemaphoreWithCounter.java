/**
* Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
