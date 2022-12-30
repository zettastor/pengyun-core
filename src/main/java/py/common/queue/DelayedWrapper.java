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

package py.common.queue;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class DelayedWrapper<E> implements Delayed {
  private final E wrappedInstance;
  private long expirationTime;

  public DelayedWrapper(E wrappedInstance, long delayMs) {
    this.wrappedInstance = wrappedInstance;
    updateDelay(delayMs);
  }

  public E getWrappedInstance() {
    return wrappedInstance;
  }

  public void updateDelay(long delayMs) {
    this.expirationTime = System.currentTimeMillis() + delayMs;
  }

  @Override
  public long getDelay(@Nonnull TimeUnit unit) {
    long delayMs = expirationTime - System.currentTimeMillis();
    return unit.convert(delayMs, TimeUnit.MILLISECONDS);
  }

  @Override
  public int compareTo(@Nonnull Delayed o) {
    return Long.compare(getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
  }
}
