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

package py.monitor.customizable.repository;

import py.monitor.jmx.server.ResourceType;

public class ResourceMetadataIdentifier {
  private ResourceType resourceType;
  private long metadataIndex;

  public ResourceType getResourceType() {
    return resourceType;
  }

  public void setResourceType(ResourceType resourceType) {
    this.resourceType = resourceType;
  }

  public long getMetadataIndex() {
    return metadataIndex;
  }

  public void setMetadataIndex(long metadataIndex) {
    this.metadataIndex = metadataIndex;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (metadataIndex ^ (metadataIndex >>> 32));
    result = prime * result + ((resourceType == null) ? 0 : resourceType.hashCode());
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
    ResourceMetadataIdentifier other = (ResourceMetadataIdentifier) obj;
    if (metadataIndex != other.metadataIndex) {
      return false;
    }
    if (resourceType != other.resourceType) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "ResourceIdentifier [resourceType=" + resourceType + ", resourceMetadataIndex="
        + metadataIndex
        + "]";
  }

}