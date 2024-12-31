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
