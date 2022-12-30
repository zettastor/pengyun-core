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

