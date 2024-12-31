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
