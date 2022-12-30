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

package py.engine;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DelayedTask extends AbstractTask {
  private final Logger logger = LoggerFactory.getLogger(DelayedTask.class);

  private long delayMs;
  private long origin;

  public DelayedTask(int delayMs) {
    this.origin = System.currentTimeMillis();
    this.delayMs = delayMs;
  }

  protected long getOrigin() {
    return origin;
  }

  public void updateDelay(long newDelayMs) {
    long now = System.currentTimeMillis();
    if (now + newDelayMs >= delayMs + origin) {
      delayMs = newDelayMs;
      origin = now;
    }
  }

  @Override
  public long getDelay(TimeUnit unit) {
    return unit.convert(origin + delayMs - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public final int compareTo(Delayed delayed) {
    logger.trace("this={}, delayed={}", this, delayed);
    if (delayed == null) {
      return 1;
    }

    if (delayed == this) {
      return 0;
    }

    long d = (getDelay(TimeUnit.MILLISECONDS) - delayed.getDelay(TimeUnit.MILLISECONDS));
    return ((d == 0) ? 0 : ((d < 0) ? -1 : 1));
  }

  @Override
  public abstract Result work();

  @Override
  public String toString() {
    return "DelayedTask{" + "origin=" + origin + ", delayMs=" + delayMs + '}';
  }
}
