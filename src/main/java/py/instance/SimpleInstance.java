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

import py.common.struct.EndPoint;

public class SimpleInstance {
  private final InstanceId instanceId;
  private final EndPoint endPoint;

  public SimpleInstance(InstanceId instanceId, EndPoint endPoint) {
    this.instanceId = instanceId;
    this.endPoint = endPoint;
  }

  public InstanceId getInstanceId() {
    return instanceId;
  }

  public EndPoint getEndPoint() {
    return endPoint;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SimpleInstance)) {
      return false;
    }

    SimpleInstance that = (SimpleInstance) o;

    return instanceId != null ? instanceId.equals(that.instanceId) : that.instanceId == null;
  }

  @Override
  public int hashCode() {
    return instanceId != null ? instanceId.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "SimpleInstance{" + "instanceId=" + instanceId + ", endPoint=" + endPoint + '}';
  }
}
