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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import py.common.RequestIdBuilder;

public class EventDataInfo {
  private long startTime;
  private String hostName;
  private String operation;
  private Map<String, AtomicLong> counters;
  private String remoteAddr;
  private String program;
  private EventLogInfo eventLogInfo;
  private Map<String, String> nameValues;

  private AtomicInteger validEventDataCount;

  public EventDataInfo() {
    counters = new HashMap<>();
    nameValues = new HashMap<>();
    validEventDataCount = new AtomicInteger(0);
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public Map<String, AtomicLong> getCounters() {
    return counters;
  }

  public void setCounters(Map<String, AtomicLong> counters) {
    this.counters = counters;
  }

  public void putCounter(String counterKey, long counterValue) {
    this.counters.put(counterKey, new AtomicLong(counterValue));
  }

  public EventLogInfo getEventLogInfo() {
    return eventLogInfo;
  }

  public void setEventLogInfo(EventLogInfo eventLogInfo) {
    this.eventLogInfo = eventLogInfo;
  }

  public long getCounter(String counterKey) {
    AtomicLong counterValue = this.counters.get(counterKey);
    if (counterValue == null) {
      return -1;
    } else {
      return counterValue.get();
    }
  }

  public synchronized long addAndGetCounter(String counterKey, long counterValue) {
    return this.counters.get(counterKey).addAndGet(counterValue);
  }

  public String getRemoteAddr() {
    return remoteAddr;
  }

  public void setRemoteAddr(String remoteAddr) {
    this.remoteAddr = remoteAddr;
  }

  public String getProgram() {
    return program;
  }

  public void setProgram(String program) {
    this.program = program;
  }

  public Map<String, String> getNameValues() {
    return nameValues;
  }

  public void setNameValues(Map<String, String> nameValues) {
    this.nameValues = nameValues;
  }

  public void addNameValue(String name, String value) {
    this.nameValues.put(name, value);
  }

  ;

  public AtomicInteger getValidEventDataCount() {
    return validEventDataCount;
  }

  public void setValidEventDataCount(AtomicInteger validEventDataCount) {
    this.validEventDataCount = validEventDataCount;
  }

  public int incValidEventDataCount() {
    return this.validEventDataCount.incrementAndGet();
  }

  public EventDataInfo deepClone() {
    EventDataInfo eventDataInfo = new EventDataInfo();
    eventDataInfo.setStartTime(this.getStartTime());
    eventDataInfo.setRemoteAddr(this.remoteAddr);
    eventDataInfo.setProgram(this.program);
    eventDataInfo.setHostName(this.hostName);
    eventDataInfo.setOperation(this.operation);
    eventDataInfo.setEventLogInfo(this.eventLogInfo);
    Map<String, AtomicLong> counters = new HashMap<>();
    for (Map.Entry<String, AtomicLong> entry : this.counters.entrySet()) {
      String counterKey = entry.getKey();
      AtomicLong counterValue = new AtomicLong(entry.getValue().get());
      counters.put(counterKey, counterValue);
    }
    Map<String, String> nameValues = new HashMap<>(this.nameValues);
    AtomicInteger validEventDataCount = new AtomicInteger(this.validEventDataCount.get());
    eventDataInfo.setValidEventDataCount(validEventDataCount);
    eventDataInfo.setCounters(counters);
    eventDataInfo.setNameValues(nameValues);

    return eventDataInfo;
  }

  public EventLogInfo buildFromEventDataInfo(Set<String> userDefineKeys, String operationName) {
    String ctrf = System.getProperty("line.separator", "\n");
    String eoq = "EOQ";
    EventLogInfo eventLogInfo = new EventLogInfo();
    Date startTime = new Date(System.currentTimeMillis());
    Date endTime = new Date(System.currentTimeMillis());
    String eventLogStr = "StartTime=" + startTime + ctrf
        + "EndTime=" + endTime + ctrf
        + "Time=0 msecs" + ctrf
        + "Hostname=" + this.getHostName() + ctrf
        + "Timing=" + ctrf
        + "Counters=" + this.getCounters().toString() + ctrf
        + "Info=" + operationName + ctrf
        + "REMOTE_ADDR=" + this.getRemoteAddr() + ctrf
        + "Program=MonitorServer" + ctrf
        + "History=" + ctrf
        + "Operation=" + operationName + ctrf;
    for (String key : userDefineKeys) {
      eventLogStr = eventLogStr
          + key + "="
          + this.getNameValues().get(key) + ctrf;
    }
    eventLogStr = eventLogStr
        + "Instant"
        + "true" + ctrf
        + "EOQ" + ctrf;
    eventLogInfo.setId(RequestIdBuilder.get());
    eventLogInfo.setStartTime(startTime);
    eventLogInfo.setEventLog(eventLogStr);
    this.setEventLogInfo(eventLogInfo);
    return eventLogInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    EventDataInfo that = (EventDataInfo) o;

    if (startTime != that.startTime) {
      return false;
    }
    if (hostName != null ? !hostName.equals(that.hostName) : that.hostName != null) {
      return false;
    }
    if (operation != null ? !operation.equals(that.operation) : that.operation != null) {
      return false;
    }
    if (counters != null ? !counters.equals(that.counters) : that.counters != null) {
      return false;
    }
    if (remoteAddr != null ? !remoteAddr.equals(that.remoteAddr) : that.remoteAddr != null) {
      return false;
    }
    if (program != null ? !program.equals(that.program) : that.program != null) {
      return false;
    }
    return nameValues != null ? nameValues.equals(that.nameValues) : that.nameValues == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (startTime ^ (startTime >>> 32));
    result = 31 * result + (hostName != null ? hostName.hashCode() : 0);
    result = 31 * result + (operation != null ? operation.hashCode() : 0);
    result = 31 * result + (counters != null ? counters.hashCode() : 0);
    result = 31 * result + (remoteAddr != null ? remoteAddr.hashCode() : 0);
    result = 31 * result + (program != null ? program.hashCode() : 0);
    result = 31 * result + (nameValues != null ? nameValues.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "EventDataInfo{" + "startTime=" + startTime + ", hostName='" + hostName + '\''
        + ", operation='"
        + operation + '\'' + ", counters=" + counters + ", remoteAddr='" + remoteAddr + '\''
        + ", program='"
        + program + '\'' + ", eventLogInfo=" + eventLogInfo + ", nameValues=" + nameValues
        + ", validEventDataCount=" + validEventDataCount + '}';
  }
}

