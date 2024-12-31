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

public class PerformanceSearchTemplate {
  private long id;
  private String name;
  private Date startTime;
  private Date endTime;
  private long period;
  private String timeUnit;
  private String counterKeyJson;
  private String sourcesJson;
  private long accountId;
  private String objectType;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public long getPeriod() {
    return period;
  }

  public void setPeriod(long period) {
    this.period = period;
  }

  public String getTimeUnit() {
    return timeUnit;
  }

  public void setTimeUnit(String timeUnit) {
    this.timeUnit = timeUnit;
  }

  public String getCounterKeyJson() {
    return counterKeyJson;
  }

  public void setCounterKeyJson(String counterKeyJson) {
    this.counterKeyJson = counterKeyJson;
  }

  public String getSourcesJson() {
    return sourcesJson;
  }

  public void setSourcesJson(String sourcesJson) {
    this.sourcesJson = sourcesJson;
  }

  public String getObjectType() {
    return objectType;
  }

  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  public long getAccountId() {
    return accountId;
  }

  public void setAccountId(long accountId) {
    this.accountId = accountId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PerformanceSearchTemplate)) {
      return false;
    }

    PerformanceSearchTemplate that = (PerformanceSearchTemplate) o;

    if (id != that.id) {
      return false;
    }
    if (period != that.period) {
      return false;
    }
    if (accountId != that.accountId) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) {
      return false;
    }
    if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) {
      return false;
    }
    if (timeUnit != null ? !timeUnit.equals(that.timeUnit) : that.timeUnit != null) {
      return false;
    }
    if (counterKeyJson != null ? !counterKeyJson.equals(that.counterKeyJson)
        : that.counterKeyJson != null) {
      return false;
    }
    if (sourcesJson != null ? !sourcesJson.equals(that.sourcesJson) : that.sourcesJson != null) {
      return false;
    }
    return objectType != null ? objectType.equals(that.objectType) : that.objectType == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (id ^ (id >>> 32));
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
    result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
    result = 31 * result + (int) (period ^ (period >>> 32));
    result = 31 * result + (timeUnit != null ? timeUnit.hashCode() : 0);
    result = 31 * result + (counterKeyJson != null ? counterKeyJson.hashCode() : 0);
    result = 31 * result + (sourcesJson != null ? sourcesJson.hashCode() : 0);
    result = 31 * result + (int) (accountId ^ (accountId >>> 32));
    result = 31 * result + (objectType != null ? objectType.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "PerformanceSearchTemplate{"
        + "id=" + id
        + ", name='" + name + '\''
        + ", startTime=" + startTime
        + ", endTime=" + endTime
        + ", period=" + period
        + ", timeUnit='" + timeUnit + '\''
        + ", counterKeyJson='" + counterKeyJson + '\''
        + ", sourcesJson='" + sourcesJson + '\''
        + ", accountId=" + accountId
        + ", objectType='" + objectType + '\''
        + '}';
  }
}
