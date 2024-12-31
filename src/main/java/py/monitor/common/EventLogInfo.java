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
import java.util.Objects;

public class EventLogInfo implements Comparable<EventLogInfo> {
  private long id;
  private String eventLog;
  private Date startTime;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getEventLog() {
    return eventLog;
  }

  public void setEventLog(String eventLog) {
    this.eventLog = eventLog;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventLogInfo that = (EventLogInfo) o;
    return id == that.id && Objects.equals(eventLog, that.eventLog) && Objects
        .equals(startTime, that.startTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, eventLog, startTime);
  }

  @Override
  public String toString() {
    return "EventLogInfo{" + "id=" + id + ", eventLog='" + eventLog + '\'' + ", startTime="
        + startTime + '}';
  }

  @Override
  public int compareTo(EventLogInfo o) {
    return this.startTime.compareTo(o.startTime);
  }
}

