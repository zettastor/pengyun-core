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

package py.monitor.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import py.common.Utils;

public class TimeSpan implements Serializable {
  @JsonIgnore
  private static final long serialVersionUID = 1015193438021285786L;

  private long startTime;
  private long stopTime;

  public TimeSpan(long startTime, long stopTime) {
    super();
    this.startTime = startTime;
    this.stopTime = stopTime;
  }

  public TimeSpan() {
    super();
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getStopTime() {
    return stopTime;
  }

  public void setStopTime(long stopTime) {
    this.stopTime = stopTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (startTime ^ (startTime >>> 32));
    result = prime * result + (int) (stopTime ^ (stopTime >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    TimeSpan other = (TimeSpan) obj;
    if (startTime != other.startTime) {
      return false;
    }
    if (stopTime != other.stopTime) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "TimeSpan [startTime=" + "(" + startTime + ")" + Utils.millsecondToString(startTime)
        + ", stopTime="
        + "(" + stopTime + ")" + Utils.millsecondToString(stopTime) + "]";
  }

}
