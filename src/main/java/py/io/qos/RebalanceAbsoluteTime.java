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

package py.io.qos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Set;

public class RebalanceAbsoluteTime {
  private long id;
  private long beginTime;
  private long endTime;
  private Set<WeekDay> weekDaySet;

  public static RebalanceAbsoluteTime fromJson(String json) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(json, RebalanceAbsoluteTime.class);
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getBeginTime() {
    return beginTime;
  }

  public void setBeginTime(long beginTime) {
    this.beginTime = beginTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public Set<WeekDay> getWeekDaySet() {
    return weekDaySet;
  }

  public void setWeekDaySet(Set<WeekDay> weekDaySet) {
    this.weekDaySet = weekDaySet;
  }

  public String toJsonString() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RebalanceAbsoluteTime that = (RebalanceAbsoluteTime) o;

    if (id != that.id) {
      return false;
    }
    if (beginTime != that.beginTime) {
      return false;
    }
    if (endTime != that.endTime) {
      return false;
    }
    return weekDaySet != null ? weekDaySet.equals(that.weekDaySet) : that.weekDaySet == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (id ^ (id >>> 32));
    result = 31 * result + (int) (beginTime ^ (beginTime >>> 32));
    result = 31 * result + (int) (endTime ^ (endTime >>> 32));
    result = 31 * result + (weekDaySet != null ? weekDaySet.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "RebalanceAbsoluteTime{"
        + "id=" + id
        + ", beginTime=" + beginTime
        + ", endTime=" + endTime
        + ", weekDaySet=" + weekDaySet
        + '}';
  }

  public enum WeekDay {
    SUN(0),
    MON(1),
    TUE(2),
    WED(3),
    THU(4),
    FRI(5),
    SAT(6);

    int value;

    WeekDay(int value) {
      if (value < 0 || value > 6) {
        value = 0;
      }
      this.value = value;
    }

    public static WeekDay findByValue(int value) {
      switch (value) {
        case 0:
          return SUN;
        case 1:
          return MON;
        case 2:
          return TUE;
        case 3:
          return WED;
        case 4:
          return THU;
        case 5:
          return FRI;
        case 6:
          return SAT;
        default:
          return null;
      }
    }

    public int getValue() {
      return value;
    }
  }
}
