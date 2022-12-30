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

package py.common.struct;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Collection;
import py.exception.InvalidFormatException;

public class EndPoint implements Serializable {
  private static final long serialVersionUID = 1L;
  private int port;
  private String hostName;

  public EndPoint() {
  }

  public EndPoint(EndPoint copyFrom) {
    this.hostName = new String(copyFrom.hostName);
    this.port = copyFrom.port;
  }

  @JsonCreator
  public EndPoint(@JsonProperty("hostName") String host, @JsonProperty("port") int port) {
    this.hostName = host;
    this.port = port;
  }

  @JsonIgnore
  public EndPoint(@JsonProperty("hostName") String endPointFormatString)
      throws InvalidFormatException {
    String[] tokens = endPointFormatString.split(":");
    if (tokens == null || tokens.length != 2) {
      throw new InvalidFormatException("Can't parse as an endpoint: " + endPointFormatString);
    } else {
      this.hostName = tokens[0];
      this.port = Integer.valueOf(tokens[1]).intValue();
    }
  }

  public static EndPoint fromString(String connection) throws InvalidFormatException {
    if (connection != null) {
      String[] parts = connection.split(":");
      if (parts.length == 2) {
        return new EndPoint(parts[0], Integer.parseInt(parts[1]));
      }
    }
    throw new InvalidFormatException("Can't parse to an endpoint: " + connection);
  }

  /**
   * Converts a collection of strings to an array of EndPoints method.
   *
   * @param strings Colleciton of strings.
   * @return Array of EndPoint
   */
  public static EndPoint[] convertCollectionOfStringToEndPoints(Collection<String> strings) {
    EndPoint[] endpoints = new EndPoint[strings.size()];
    int i = 0;
    for (String string : strings) {
      endpoints[i++] = fromString(string);
    }
    return endpoints;
  }

  @Override
  public EndPoint clone() {
    return new EndPoint(hostName, port);
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  @JsonIgnore
  public InetSocketAddress getInetSocketAddress() {
    return new InetSocketAddress(hostName, port);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
    result = prime * result + port;
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
    EndPoint other = (EndPoint) obj;
    if (hostName == null) {
      if (other.hostName != null) {
        return false;
      }
    } else if (!hostName.equals(other.hostName)) {
      return false;
    }
    if (port != other.port) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return hostName + ":" + port;
  }
}
