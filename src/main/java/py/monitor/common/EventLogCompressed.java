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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import py.common.RequestIdBuilder;

public class EventLogCompressed {
  private String id;
  private AtomicLong startTime;
  private AtomicLong endTime;
  private String counterKey;
  private String sourceId;
  private AtomicLong counterTotal;
  private AtomicInteger frequency;
  private String operation;

  public EventLogCompressed() {
    id = String.valueOf(RequestIdBuilder.get());
    startTime = new AtomicLong();
    endTime = new AtomicLong();
    counterTotal = new AtomicLong();
    frequency = new AtomicInteger();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public AtomicLong getStartTime() {
    return startTime;
  }

  public void setStartTime(AtomicLong startTime) {
    this.startTime = startTime;
  }

  public AtomicLong getEndTime() {
    return endTime;
  }

  public void setEndTime(AtomicLong endTime) {
    this.endTime = endTime;
  }

  public String getCounterKey() {
    return counterKey;
  }

  public void setCounterKey(String counterKey) {
    this.counterKey = counterKey;
  }

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  public AtomicLong getCounterTotal() {
    return counterTotal;
  }

  public void setCounterTotal(AtomicLong counterTotal) {
    this.counterTotal = counterTotal;
  }

  public AtomicInteger getFrequency() {
    return frequency;
  }

  public void setFrequency(AtomicInteger frequency) {
    this.frequency = frequency;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    EventLogCompressed that = (EventLogCompressed) o;

    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) {
      return false;
    }
    if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) {
      return false;
    }
    if (counterKey != null ? !counterKey.equals(that.counterKey) : that.counterKey != null) {
      return false;
    }
    if (sourceId != null ? !sourceId.equals(that.sourceId) : that.sourceId != null) {
      return false;
    }
    if (counterTotal != null ? !counterTotal.equals(that.counterTotal)
        : that.counterTotal != null) {
      return false;
    }
    if (frequency != null ? !frequency.equals(that.frequency) : that.frequency != null) {
      return false;
    }
    return operation != null ? operation.equals(that.operation) : that.operation == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
    result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
    result = 31 * result + (counterKey != null ? counterKey.hashCode() : 0);
    result = 31 * result + (sourceId != null ? sourceId.hashCode() : 0);
    result = 31 * result + (counterTotal != null ? counterTotal.hashCode() : 0);
    result = 31 * result + (frequency != null ? frequency.hashCode() : 0);
    result = 31 * result + (operation != null ? operation.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "EventLogCompressed{" + "id='" + id + '\'' + ", startTime=" + startTime + ", endTime="
        + endTime
        + ", counterKey='" + counterKey + '\'' + ", sourceId='" + sourceId + '\''
        + ", counterTotal="
        + counterTotal + ", frequency=" + frequency + ", operation='" + operation + '\'' + '}';
  }
}
