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

package py.instance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.commons.lang3.Validate;
import py.common.struct.EndPoint;

public class Instance implements Comparable<Instance> {
  private static final byte LAST_GOSSIP_TIME_SHIFT = 8;
  private static final byte EXPIRATION_TIME_SHIFT = 9;
  private static final byte FAILED_TIME_SHIFT = 10;

  private InstanceId id;

  private Group group;

  @JsonDeserialize(using = Location.LocationDeserializer.class)
  @JsonSerialize(using = Location.LocationSerializer.class)
  private Location location;

  private String name;

  private InstanceStatus status;

  @JsonProperty("endPoints")
  private Map<PortType, EndPoint> endPoints;

  @JsonIgnore
  private long checksum;

  @JsonIgnore
  private int heartBeatCounter;

  @JsonIgnore
  private long lastGossipTime;

  @JsonIgnore
  private long expirationTime;

  private boolean netSubHealth;

  @JsonIgnore
  private long failedTime;

  @JsonIgnore
  private long ver;

  @JsonIgnore
  private long fieldBits = 0L;

  private DcType dcType;

  @JsonCreator

  public Instance(@JsonProperty("id") final InstanceId id, @JsonProperty("group") final Group group,
      @JsonProperty("name") String name, @JsonProperty("status") InstanceStatus status) {
    this.id = id;
    this.group = group;
    this.name = name;
    this.status = status;
    this.endPoints = new TreeMap<PortType, EndPoint>(new MapKeyComparator());
  }

  public Instance(final InstanceId id, final Group group, final Location location, String name,
      InstanceStatus status) {
    this.id = id;
    this.group = group;
    this.location = location;
    this.name = name;
    this.status = status;
    this.endPoints = new TreeMap<PortType, EndPoint>(new MapKeyComparator());
  }

  public Instance(InstanceId id, String name, InstanceStatus status, EndPoint endPoint) {
    this.id = id;
    this.endPoints = new TreeMap<PortType, EndPoint>(new MapKeyComparator());
    this.endPoints.put(PortType.CONTROL, endPoint);
    this.name = name;
    this.status = status;
  }

  public Instance(Instance copyFrom) {
    this.id = new InstanceId(copyFrom.id);
    this.group = (null == copyFrom.group) ? null : new Group(copyFrom.group);
    this.location = (null == copyFrom.location) ? null : new Location(copyFrom.location);
    this.endPoints = new TreeMap<PortType, EndPoint>(new MapKeyComparator());
    for (Entry<PortType, EndPoint> entry : copyFrom.getEndPoints().entrySet()) {
      endPoints.put(entry.getKey(), new EndPoint(entry.getValue()));
    }

    this.name = new String(copyFrom.name);
    this.status = copyFrom.status;
    this.checksum = copyFrom.checksum;
    this.heartBeatCounter = copyFrom.heartBeatCounter;
    this.setLastGossipTime(copyFrom.lastGossipTime);
    this.setExpirationTime(copyFrom.expirationTime);
    this.setFailedTime(copyFrom.failedTime);
    this.ver = copyFrom.ver;
    this.dcType = copyFrom.dcType;
  }

  public InstanceId getId() {
    return id;
  }

  public void setId(InstanceId id) {
    this.id = id;
  }

  public Group getGroup() {
    return group;
  }

  public void setGroup(Group group) {
    this.group = group;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<PortType, EndPoint> getEndPoints() {
    return this.endPoints;
  }

  public InstanceStatus getStatus() {
    return status;
  }

  public void setStatus(InstanceStatus status) {
    this.status = status;
  }

  public long getChecksum() {
    return checksum;
  }

  public void setChecksum(long checksum) {
    this.checksum = checksum;
  }

  public int getHeartBeatCounter() {
    return heartBeatCounter;
  }

  public void setHeartBeatCounter(int heartBeatCounter) {
    this.heartBeatCounter = heartBeatCounter;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public long getExpirationTime() {
    return expirationTime;
  }

  public void setExpirationTime(long expirationTime) {
    this.expirationTime = expirationTime;
    this.setField(EXPIRATION_TIME_SHIFT);
  }

  public boolean isNetSubHealth() {
    return netSubHealth;
  }

  public void setNetSubHealth(boolean netSubHealth) {
    this.netSubHealth = netSubHealth;
  }

  public long getFailedTime() {
    return failedTime;
  }

  public void setFailedTime(long failedTime) {
    this.failedTime = failedTime;
    this.setField(FAILED_TIME_SHIFT);
  }

  public long getVer() {
    return ver;
  }

  public void setVer(long ver) {
    this.ver = ver;
  }

  public long getLastGossipTime() {
    return lastGossipTime;
  }

  public void setLastGossipTime(long lastGossipTime) {
    this.lastGossipTime = lastGossipTime;
    this.setField(LAST_GOSSIP_TIME_SHIFT);
  }

  public DcType getDcType() {
    return dcType;
  }

  public void setDcType(DcType dcType) {
    this.dcType = dcType;
  }

  @Override
  public int compareTo(Instance other) {
    if (other == null) {
      if (this.status == InstanceStatus.FAILED || this.status == InstanceStatus.DISUSED) {
        return -1;
      } else {
        return 1;
      }
    }

    if (heartBeatCounter > other.heartBeatCounter) {
      return 1;
    } else if (heartBeatCounter < other.heartBeatCounter) {
      return -1;
    } else {
      return status.compareWith(other.status);
    }
  }

  @JsonIgnore
  public EndPoint getEndPoint() {
    return getEndPointByServiceName(PortType.CONTROL);
  }

  public void putEndPointByServiceName(PortType portType, EndPoint endPoint) {
    endPoints.put(portType, endPoint);
  }

  public EndPoint getEndPointByServiceName(PortType portType) {
    return endPoints.get(portType);
  }

  @JsonIgnore
  public boolean isSetLastGossipTime() {
    return isFieldSet(LAST_GOSSIP_TIME_SHIFT);
  }

  @JsonIgnore
  public boolean isSetExpirationTime() {
    return isFieldSet(EXPIRATION_TIME_SHIFT);
  }

  @JsonIgnore
  public boolean isSetFailedTime() {
    return isFieldSet(FAILED_TIME_SHIFT);
  }

  @Override
  public String toString() {
    return "Instance{"
        + "id=" + id
        + ", group=" + group
        + ", location=" + location
        + ", name='" + name + '\''
        + ", status=" + status
        + ", endPoints=" + endPoints
        + ", checksum=" + checksum
        + ", heartBeatCounter=" + heartBeatCounter
        + ", lastGossipTime=" + lastGossipTime
        + ", expirationTime=" + expirationTime
        + ", netSubHealth=" + netSubHealth
        + ", failedTime=" + failedTime
        + ", ver=" + ver
        + ", fieldBits=" + fieldBits
        + ", dcType=" + dcType
        + '}';
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((endPoints == null) ? 0 : endPoints.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((group == null) ? 0 : group.hashCode());
    result = prime * result + ((location == null) ? 0 : location.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
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
    Instance other = (Instance) obj;
    if (endPoints == null) {
      if (other.endPoints != null) {
        return false;
      }
    } else {
      Map<PortType, EndPoint> tmpEndPoints = other.getEndPoints();
      if (tmpEndPoints == null) {
        return false;
      } else {
        if (tmpEndPoints.size() != endPoints.size()) {
          return false;
        } else {
          for (Entry<PortType, EndPoint> entry : endPoints.entrySet()) {
            if (entry.getValue() == null) {
              if (tmpEndPoints.get(entry.getKey()) != null) {
                return false;
              }
            } else {
              if (!entry.getValue().equals(tmpEndPoints.get(entry.getKey()))) {
                return false;
              }
            }
          }
        }
      }
    }

    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }

    if (group == null) {
      if (other.getGroup() != null) {
        return false;
      }
    } else if (!group.equals(other.getGroup())) {
      return false;
    }

    if (location == null) {
      if (other.location != null) {
        return false;
      }
    } else if (!location.equals(other.location)) {
      return false;
    }

    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (status != other.status) {
      return false;
    }
    return true;
  }


  private int addStringToByteArray(String s, byte[] bytes, int offset) {
    int n = s.length();
    for (int i = 0; i < n; i++, offset++) {
      bytes[offset] = (byte) s.charAt(i);
    }
    return offset;
  }

  private int addLongToByteArray(long n, byte[] bytes, int offset) {
    for (int i = 0; i < Long.BYTES; i++, offset++) {
      bytes[offset] = (byte) (n & 0xff);
      n >>>= 8;
    }

    Validate.isTrue(n == 0);
    return offset;
  }

  private int addIntegerToByteArray(int n, byte[] bytes, int offset) {
    for (int i = 0; i < Integer.BYTES; i++, offset++) {
      bytes[offset] = (byte) (n & 0xff);
      n >>>= 8;
    }

    Validate.isTrue(n == 0);
    return offset;
  }

  private int addBooleanToByteArray(boolean value, byte[] bytes, int offset) {
    bytes[offset] = (value) ? (byte) 0xff : (byte) 0x00;
    offset++;

    return offset;
  }

  private boolean isFieldSet(byte shift) {
    return (fieldBits & (1L << shift)) != 0;
  }

  private void setField(byte shift) {
    fieldBits |= (1L << shift);
  }

  class MapKeyComparator implements Comparator<PortType> {
    @Override
    public int compare(PortType port1, PortType port2) {
      return port1.compareTo(port2);
    }
  }
}
