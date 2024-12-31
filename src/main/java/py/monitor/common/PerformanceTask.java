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

import py.common.RequestIdBuilder;

public class PerformanceTask {
  private String id;
  private String counterKey;
  private String sourceId;
  private String sourceName;
  private long startTime;
  private boolean status;

  public PerformanceTask() {
    this.id = String.valueOf(RequestIdBuilder.get());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public boolean isStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

  public String getSourceName() {
    return sourceName;
  }

  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PerformanceTask that = (PerformanceTask) o;

    if (startTime != that.startTime) {
      return false;
    }
    if (status != that.status) {
      return false;
    }
    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (counterKey != null ? !counterKey.equals(that.counterKey) : that.counterKey != null) {
      return false;
    }
    return sourceId != null ? sourceId.equals(that.sourceId) : that.sourceId == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (counterKey != null ? counterKey.hashCode() : 0);
    result = 31 * result + (sourceId != null ? sourceId.hashCode() : 0);
    result = 31 * result + (int) (startTime ^ (startTime >>> 32));
    result = 31 * result + (status ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return "PerformanceTask{" + "id='" + id + '\'' + ", counterKey='" + counterKey + '\''
        + ", sourceId='"
        + sourceId + '\'' + ", sourceName='" + sourceName + '\'' + ", startTime=" + startTime
        + ", status="
        + status + '}';
  }
}
