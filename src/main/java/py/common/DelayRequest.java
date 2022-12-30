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

package py.common;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayRequest implements Delayed {
  private long delay;
  private long timeSettingDelay;

  public DelayRequest(long delay) {
    this.delay = delay;
    this.timeSettingDelay = System.currentTimeMillis();
  }

  /**
   * Only update the delay to new value when the new one is larger than the current one.
   *
   * @param newDelay (ms)
   */
  public void updateDelay(long newDelay) {
    long now = System.currentTimeMillis();
    if (now + newDelay > getExpireTime()) {
      delay = newDelay;
      timeSettingDelay = now;
    }
  }

  /**
   * update timeSettingDelay with current delay.
   */
  public void refreshDelay() {
    updateDelay(this.delay);
  }

  private long getExpireTime() {
    return delay + timeSettingDelay;
  }

  /**
   * Update the delay no matter what.
   */
  public void updateDelayWithForce(long newDelay) {
    delay = newDelay;
    timeSettingDelay = System.currentTimeMillis();
  }

  @Override
  public long getDelay(TimeUnit unit) {
    // internally the delay is in milliseconds
    return unit.convert(getExpireTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
  }

  public long getDelay() {
    return delay;
  }

  @Override
  public int compareTo(Delayed delayed) {
    if (delayed == null) {
      return 1;
    }

    if (delayed == this) {
      return 0;
    }

    long d = (getDelay(TimeUnit.MILLISECONDS) - delayed.getDelay(TimeUnit.MILLISECONDS));
    return ((d == 0) ? 0 : ((d < 0) ? -1 : 1));
  }

}
