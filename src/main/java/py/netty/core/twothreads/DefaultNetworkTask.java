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

package py.netty.core.twothreads;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultNetworkTask implements NetworkTask {
  static final Logger logger = LoggerFactory.getLogger(DefaultNetworkTask.class);
  private long delayed;
  private TimeUnit timeUnit;
  private Runnable runnable;

  public DefaultNetworkTask(Runnable runnable, long delayed, TimeUnit timeUnit) {
    if (runnable == null) {
      throw new IllegalArgumentException("runnable is null");
    }

    if (delayed < 0) {
      throw new IllegalArgumentException("delay is negative");
    }

    if (timeUnit == null) {
      throw new IllegalArgumentException("time unit is null");
    }
    this.runnable = runnable;
    this.timeUnit = timeUnit;
    this.delayed = delayed;
  }

  @Override
  public long getDelay(TimeUnit unit) {
    return unit.convert(delayed, timeUnit);
  }

  @Override
  public int compareTo(Delayed d) {
    if (d == null) {
      return 1;
    }

    if (d == this) {
      return 0;
    }

    long diff = (getDelay(timeUnit) - d.getDelay(timeUnit));
    return ((diff == 0) ? 0 : ((diff < 0) ? -1 : 1));
  }

  @Override
  public void run() {
    try {
      runnable.run();
    } catch (Throwable t) {
      logger.error("runnable:" + runnable + " throws an exception", t);
    }
  }
}
