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
import java.sql.Blob;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.license.Utils;

public class IoLimitation {
  private static final Logger logger = LoggerFactory.getLogger(IoLimitation.class);

  private long id;

  private String name;

  private LimitType limitType;

  private List<IoLimitationEntry> entries;

  private IoLimitationStatus status;

  public IoLimitation() {
  }

  public IoLimitation(IoLimitationInformation ioLimitationInformation) {
    this.id = ioLimitationInformation.getRuleId();
    this.name = ioLimitationInformation.getIoLimitationName();

    if (ioLimitationInformation.getLimitType() != null) {
      this.limitType = LimitType.valueOf(ioLimitationInformation.getLimitType());
    }

    if (ioLimitationInformation.getStatus() != null) {
      this.status = IoLimitationStatus.valueOf(ioLimitationInformation.getStatus());
    }

    try {
      if (ioLimitationInformation.getIoLimitationEntriesJson() != null) {
        this.entries = ioLimitationInformation.parseIoLimitationEntries(
            new String(Utils.readFrom(ioLimitationInformation.getIoLimitationEntriesJson())));
      }
    } catch (Exception e) {
      logger.error("caught an exception", e);
    }
  }

  public IoLimitation(long id, String name, int upperLimitedIops, int lowerLimitedIops,
      long upperLimitedThroughput,
      long lowerLimitedThroughput, LocalTime startTime, LocalTime endTime) {
    this.limitType = LimitType.Dynamic;
    this.id = id;
    this.name = name;
    this.status = IoLimitationStatus.AVAILABLE;

    List<IoLimitationEntry> entries = new ArrayList<>();
    entries
        .add(new IoLimitationEntry(id, upperLimitedIops, lowerLimitedIops, upperLimitedThroughput,
            lowerLimitedThroughput, startTime, endTime));

    this.entries = entries;
  }

  public LimitType getLimitType() {
    return limitType;
  }

  public void setLimitType(LimitType limitType) {
    this.limitType = limitType;
  }

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

  public IoLimitationStatus getStatus() {
    return status;
  }

  public void setStatus(IoLimitationStatus status) {
    this.status = status;
  }

  public List<IoLimitationEntry> getEntries() {
    return entries;
  }

  public void setEntries(List<IoLimitationEntry> entries) {
    this.entries = entries;
  }

  public IoLimitationInformation toIoLimitationInformation(IoLimitationStore ioLimitationStore) {
    IoLimitationInformation ioLimitation = new IoLimitationInformation();
    ioLimitation.setRuleId(id);
    ioLimitation.setIoLimitationName(name);
    if (limitType != null) {
      ioLimitation.setLimitType(limitType.name());
    }
    if (status != null) {
      ioLimitation.setStatus(status.name());
    }

    try {
      JSONArray ioLimitationEntries = new JSONArray();
      if (entries == null) {
        logger.error("cannot parse the entries null");
        return ioLimitation;
      }

      for (IoLimitationEntry entry : entries) {
        ioLimitationEntries.add(entry.toJsonString());
      }

      byte[] ioLimitationEntriesByte = ioLimitationEntries.toString().getBytes();
      Blob ioLimitationEntriesJson = ioLimitationStore.createBlob(ioLimitationEntriesByte);

      logger.debug("toIOLimitationInformation ioLimitationEntries {} {}", ioLimitationEntries,
          ioLimitationEntriesJson);
      ioLimitation.setIoLimitationEntriesJson(ioLimitationEntriesJson);
    } catch (JsonProcessingException e) {
      logger.error("cannot parse the driver meta data to driver info", e);
    }
    return ioLimitation;
  }

  public boolean validate() {
    for (IoLimitationEntry entry : entries) {
      if (((entry.getLowerLimitedIops() < -1) || (entry.getUpperLimitedIops() < -1)
          || (entry.getLowerLimitedThroughput() < -1) || (entry.getUpperLimitedThroughput()
          < -1))) {
        return false;
      }
      if (entry.getLowerLimitedIops() > entry.getUpperLimitedIops()
          && entry.getUpperLimitedIops() > 0) {
        return false;
      }
      if (entry.getLowerLimitedThroughput() > entry.getUpperLimitedThroughput()
          && entry.getUpperLimitedThroughput() > 0) {
        return false;
      }
      if (limitType == LimitType.Dynamic) {
        if (entry.getStartTimeWithLocalFormat().equals(entry.getEndTimeWithLocalFormat())
            || entry.getStartTimeWithLocalFormat().isAfter(entry.getEndTimeWithLocalFormat())) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IoLimitation)) {
      return false;
    }

    IoLimitation that = (IoLimitation) o;

    if (id != that.id) {
      return false;
    }
    if (name != that.name) {
      return false;
    }
    if (limitType != that.limitType) {
      return false;
    }
    if (entries != null ? !entries.equals(that.entries) : that.entries != null) {
      return false;
    }
    return status != null ? status.equals(that.status) : that.status == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (id ^ (id >>> 32));
    result = 31 * result + (limitType != null ? limitType.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (entries != null ? entries.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "IOLimitation [id=" + id + ", name=" + name + ", limitType=" + limitType
        + ", entries=" + entries + "]";
  }

  public enum LimitType {
    Static, Dynamic
  }
}