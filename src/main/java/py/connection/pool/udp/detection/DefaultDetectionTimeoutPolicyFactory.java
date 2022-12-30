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

package py.connection.pool.udp.detection;

import java.util.concurrent.atomic.AtomicInteger;

public class DefaultDetectionTimeoutPolicyFactory implements DetectionTimeoutPolicyFactory {
  private final int maxRetryTimes;
  private final long initTimeoutMs;

  public DefaultDetectionTimeoutPolicyFactory(int maxRetryTimes, long initTimeoutMs) {
    this.maxRetryTimes = maxRetryTimes;
    this.initTimeoutMs = initTimeoutMs;
  }

  @Override
  public DetectionTimeoutPolicy generate() {
    return new DetectionTimeoutPolicy() {
      AtomicInteger currentRetryTimes = new AtomicInteger(1);

      @Override
      public long getTimeoutMs() {
        int time = currentRetryTimes.getAndIncrement();

        if (time > maxRetryTimes) {
          return -1;
        }

        return initTimeoutMs * time;
      }
    };
  }

}
