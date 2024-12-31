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