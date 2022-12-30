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

package py.common;

import py.common.log.info.carrier.LogInfoCarrier;

public class LoggerEvent {
  private TraceType traceType;
  private long traceId;

  private LogInfoCarrier logInfoCarrier;
  private TraceAction traceAction;
  private String className;
  private int timeoutMs;
  private long endTime;

  public LoggerEvent() {
  }

  public void deepCopy(LoggerEvent other) {
    if (other != null) {
      if (other.getTraceType() != null) {
        this.traceType = other.getTraceType();
      }

      this.traceId = other.getTraceId();

      if (other.getLogInfoCarrier() != null) {
        this.logInfoCarrier = other.getLogInfoCarrier();
      }
      if (other.getTraceAction() != null) {
        this.traceAction = other.getTraceAction();
      }
      if (other.getClassName() != null) {
        this.className = other.getClassName();
      }
      this.timeoutMs = other.getTimeoutMs();
      this.endTime = other.getEndTime();
    }
  }

  public TraceType getTraceType() {
    return traceType;
  }

  public void setTraceType(TraceType traceType) {
    this.traceType = traceType;
  }

  public long getTraceId() {
    return traceId;
  }

  public void setTraceId(long traceId) {
    this.traceId = traceId;
  }

  public TraceAction getTraceAction() {
    return traceAction;
  }

  public void setTraceAction(TraceAction traceAction) {
    this.traceAction = traceAction;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public int getTimeoutMs() {
    return timeoutMs;
  }

  public void setTimeoutMs(int timeoutMs) {
    this.timeoutMs = timeoutMs;
  }

  public boolean isMark() {
    return traceType == TraceType.MARK;
  }

  public boolean isDoneTrace() {
    return traceType == TraceType.DONETRACE;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public LogInfoCarrier getLogInfoCarrier() {
    return logInfoCarrier;
  }

  public void setLogInfoCarrier(LogInfoCarrier logInfoCarrier) {
    this.logInfoCarrier = logInfoCarrier;
  }

  public String formatLogger() {
    Long currentTimeMillis = System.currentTimeMillis();
    String timeToPrint = Utils.millsecondToString(currentTimeMillis);

    // format string
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("[");
    stringBuilder.append(timeToPrint);
    stringBuilder.append("]");
    stringBuilder.append(" [");
    stringBuilder.append(className);
    stringBuilder.append("]");
    stringBuilder.append(logInfoCarrier.buildLogInfo());

    return stringBuilder.toString();
  }

  public void release() {
    if (logInfoCarrier != null) {
      logInfoCarrier.release();
      logInfoCarrier = null;
    }

    if (traceType != null) {
      traceType = null;
    }

    if (traceAction != null) {
      traceAction = null;
    }

    if (className != null) {
      className = null;
    }
  }

}
