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

package py.monitor.common;

import java.util.Objects;

public class PerformanceMessage {
  private long startTime;
  private String counterKey;
  private float counterValue;
  private String sourceId;

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public String getCounterKey() {
    return counterKey;
  }

  public void setCounterKey(String counterKey) {
    this.counterKey = counterKey;
  }

  public float getCounterValue() {
    return counterValue;
  }

  public void setCounterValue(float counterValue) {
    this.counterValue = counterValue;
  }

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PerformanceMessage that = (PerformanceMessage) o;

    if (startTime != that.startTime) {
      return false;
    }
    if (counterValue != that.counterValue) {
      return false;
    }
    if (counterKey != null ? !counterKey.equals(that.counterKey) : that.counterKey != null) {
      return false;
    }
    return sourceId != null ? sourceId.equals(that.sourceId) : that.sourceId == null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(startTime, counterKey, counterValue, sourceId);
  }

  @Override
  public String toString() {
    return "PerformanceMessage{" + "startTime=" + startTime + ", counterKey='" + counterKey + '\''
        + ", counterValue=" + counterValue + ", sourceId='" + sourceId + '\'' + '}';
  }
}
