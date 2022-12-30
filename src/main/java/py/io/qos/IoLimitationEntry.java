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

import static java.time.temporal.ChronoUnit.MILLIS;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoLimitationEntry implements Delayed {
  private static final Logger logger = LoggerFactory.getLogger(IoLimitationEntry.class);

  private long entryId;

  private int upperLimitedIops;

  private int lowerLimitedIops;

  private long upperLimitedThroughput;

  private long lowerLimitedThroughput;

  private LocalTime startTime;

  private LocalTime endTime;
  @JsonIgnore
  private LimitStatus limitStatus;
  @JsonIgnore
  private long delayTime;

  public IoLimitationEntry() {
    this.upperLimitedIops = -1;
    this.lowerLimitedIops = -1;
    this.upperLimitedThroughput = -1;
    this.lowerLimitedThroughput = -1;
  }

  public IoLimitationEntry(long entryId, int upperLimitedIops, int lowerLimitedIops,
      long upperLimitedThroughput,
      long lowerLimitedThroughput, LocalTime startTime, LocalTime endTime) {
    this.entryId = entryId;
    this.upperLimitedIops = upperLimitedIops;
    this.lowerLimitedIops = lowerLimitedIops;
    this.upperLimitedThroughput = upperLimitedThroughput;
    this.lowerLimitedThroughput = lowerLimitedThroughput;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public static IoLimitationEntry fromJson(String json)
      throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(json, IoLimitationEntry.class);
  }

  public int getUpperLimitedIops() {
    return upperLimitedIops;
  }

  public void setUpperLimitedIops(int upperLimitedIops) {
    this.upperLimitedIops = upperLimitedIops;
  }

  public int getLowerLimitedIops() {
    return lowerLimitedIops;
  }

  public void setLowerLimitedIops(int lowerLimitedIops) {
    this.lowerLimitedIops = lowerLimitedIops;
  }

  public long getUpperLimitedThroughput() {
    return upperLimitedThroughput;
  }

  public void setUpperLimitedThroughput(long upperLimitedThroughput) {
    this.upperLimitedThroughput = upperLimitedThroughput;
  }

  public long getLowerLimitedThroughput() {
    return lowerLimitedThroughput;
  }

  public void setLowerLimitedThroughput(long lowerLimitedThroughput) {
    this.lowerLimitedThroughput = lowerLimitedThroughput;
  }

  public boolean enableUpperIops() {
    return (upperLimitedIops > 0);
  }

  public boolean enableUpperThroughput() {
    return (upperLimitedThroughput > 0);
  }

  public boolean enableLowerIops() {
    return (lowerLimitedIops > 0);
  }

  public boolean enableLowerThroughput() {
    return (lowerLimitedThroughput > 0);
  }

  public String getStartTime() {
    return startTime.toString();
  }

  public void setStartTime(String startTime) {
    this.startTime = LocalTime.parse(startTime);
  }

  public String getEndTime() {
    return endTime.toString();
  }

  public void setEndTime(String endTime) {
    this.endTime = LocalTime.parse(endTime);
  }

  public long getEntryId() {
    return entryId;
  }

  public void setEntryId(long entryId) {
    this.entryId = entryId;
  }

  public LimitStatus getLimitStatus() {
    return limitStatus;
  }

  public void setLimitStatus(LimitStatus limitStatus) {
    this.limitStatus = limitStatus;
  }

  public long getDelayTime() {
    return delayTime;
  }

  public void setDelayTime(long delayTime) {
    this.delayTime = delayTime;
  }

  @JsonIgnore
  public LocalTime getStartTimeWithLocalFormat() {
    return startTime;
  }

  @JsonIgnore
  public LocalTime getEndTimeWithLocalFormat() {
    return endTime;
  }

  public String toJsonString() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(this);
  }

  public void update(IoLimitationEntry another) {
    if (another.getLowerLimitedIops() != -1) {
      this.setLowerLimitedIops(another.getLowerLimitedIops());
    }
    if (another.getUpperLimitedIops() != -1) {
      this.setUpperLimitedIops(another.getUpperLimitedIops());
    }
    if (another.getLowerLimitedThroughput() != -1) {
      this.setLowerLimitedThroughput(another.getLowerLimitedThroughput());
    }
    if (another.getUpperLimitedThroughput() != -1) {
      this.setUpperLimitedThroughput(another.getUpperLimitedThroughput());
    }
  }

  @Override
  public long getDelay(TimeUnit unit) {
    long delay = unit.convert(delayTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    return delay;
  }

  public IoLimitationEntry setStatusAndUpdateDelay(LimitStatus status) {
    limitStatus = status;
    LocalTime now = LocalTime.now();

    if (status == LimitStatus.Waiting) {
      if (now.isBefore(this.getStartTimeWithLocalFormat())) {
        delayTime =
            MILLIS.between(now, this.getStartTimeWithLocalFormat()) + System.currentTimeMillis();
      } else if (now.isBefore(this.getEndTimeWithLocalFormat())) {
        delayTime =
            MILLIS.between(now, this.getStartTimeWithLocalFormat()) + System.currentTimeMillis();
      } else {
        delayTime =
            24 * 3600 * 1000 + MILLIS.between(now, this.getStartTimeWithLocalFormat()) + System
                .currentTimeMillis();
      }
    } else {
      delayTime =
          MILLIS.between(now, this.getEndTimeWithLocalFormat()) + System.currentTimeMillis();
    }

    return this;
  }

  public boolean available() {
    LocalTime now = LocalTime.now();
    if (now.isAfter(getStartTimeWithLocalFormat()) && now.isBefore(getEndTimeWithLocalFormat())
        || now.equals(getStartTimeWithLocalFormat())) {
      return true;
    }
    return false;
  }

  @Override
  public int compareTo(Delayed delayed) {
    if (delayed == null) {
      return 1;
    }

    if (delayed == this) {
      return 0;
    }

    long d = (getDelay(TimeUnit.MILLISECONDS) - delayed.getDelay(TimeUnit.MILLISECONDS));
    return ((d == 0) ? 0 : ((d < 0) ? -1 : 1));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IoLimitationEntry)) {
      return false;
    }

    IoLimitationEntry that = (IoLimitationEntry) o;

    if (entryId != that.entryId) {
      return false;
    }
    if (upperLimitedIops != that.upperLimitedIops) {
      return false;
    }
    if (lowerLimitedIops != that.lowerLimitedIops) {
      return false;
    }

    if (lowerLimitedThroughput != that.lowerLimitedThroughput) {
      return false;
    }
    if (upperLimitedThroughput != that.upperLimitedThroughput) {
      return false;
    }

    if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) {
      return false;
    }

    if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = (int) (entryId ^ (entryId >>> 32));
    result = 31 * result + (int) (upperLimitedIops ^ (upperLimitedIops >>> 32));
    result = 31 * result + (int) (lowerLimitedIops ^ (lowerLimitedIops >>> 32));
    result = 31 * result + (int) (lowerLimitedThroughput ^ (lowerLimitedThroughput >>> 32));
    result = 31 * result + (int) (upperLimitedThroughput ^ (upperLimitedThroughput >>> 32));
    result = 31 * result + (int) (entryId ^ (entryId >>> 32));
    result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
    result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
    return result;
  }

  @JsonIgnore
  public boolean isExpired() {
    LocalTime now = LocalTime.now();
    return now.equals(getEndTimeWithLocalFormat()) || now.isAfter(getEndTimeWithLocalFormat());
  }

  @Override
  public String toString() {
    return "IOLimitationEntry{" + "entryId=" + entryId + ", upperLimitedIOPS=" + upperLimitedIops
        + ", lowerLimitedIOPS=" + lowerLimitedIops + ", upperLimitedThroughput="
        + upperLimitedThroughput
        + ", lowerLimitedThroughput=" + lowerLimitedThroughput + ", startTime=" + startTime
        + ", endTime="
        + endTime + ", limitStatus=" + limitStatus + ", delayTime=" + delayTime + '}';
  }

  public enum LimitStatus {
    Waiting, Working
  }
}