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
