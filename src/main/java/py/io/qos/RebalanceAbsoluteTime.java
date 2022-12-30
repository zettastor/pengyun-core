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
