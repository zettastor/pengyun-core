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
