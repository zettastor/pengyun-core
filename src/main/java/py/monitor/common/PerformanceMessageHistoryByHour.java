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

package py.monitor.common;

import java.util.Date;

public class PerformanceMessageHistoryByHour {
  private String id;
  private String sourceId;
  private String counterKey;
  private long counterTotal;
  private int frequency;
  private Date hour;
  private String operation;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  public String getCounterKey() {
    return counterKey;
  }

  public void setCounterKey(String counterKey) {
    this.counterKey = counterKey;
  }

  public long getCounterTotal() {
    return counterTotal;
  }

  public void setCounterTotal(long counterTotal) {
    this.counterTotal = counterTotal;
  }

  public int getFrequency() {
    return frequency;
  }

  public void setFrequency(int frequency) {
    this.frequency = frequency;
  }

  public Date getHour() {
    return hour;
  }

  public void setHour(Date hour) {
    this.hour = hour;
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

    PerformanceMessageHistoryByHour that = (PerformanceMessageHistoryByHour) o;

    if (counterTotal != that.counterTotal) {
      return false;
    }
    if (frequency != that.frequency) {
      return false;
    }
    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (sourceId != null ? !sourceId.equals(that.sourceId) : that.sourceId != null) {
      return false;
    }
    if (counterKey != null ? !counterKey.equals(that.counterKey) : that.counterKey != null) {
      return false;
    }
    if (hour != null ? !hour.equals(that.hour) : that.hour != null) {
      return false;
    }
    return operation != null ? operation.equals(that.operation) : that.operation == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (sourceId != null ? sourceId.hashCode() : 0);
    result = 31 * result + (counterKey != null ? counterKey.hashCode() : 0);
    result = 31 * result + (int) (counterTotal ^ (counterTotal >>> 32));
    result = 31 * result + frequency;
    result = 31 * result + (hour != null ? hour.hashCode() : 0);
    result = 31 * result + (operation != null ? operation.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "PerformanceMessageHistory{" + "id='" + id + '\'' + ", sourceId='" + sourceId + '\''
        + ", counterKey='"
        + counterKey + '\'' + ", counterTotal=" + counterTotal + ", frequency=" + frequency
        + ", hour="
        + hour + ", operation='" + operation + '\'' + '}';
  }
}
