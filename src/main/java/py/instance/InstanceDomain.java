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

package py.instance;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class InstanceDomain {
  private Long domainId;

  public InstanceDomain() {
    this.domainId = null;
  }

  public InstanceDomain(Long domainId) {
    this.setDomainId(domainId);
  }

  public InstanceDomain(InstanceDomain other) {
    this.domainId = other.domainId;
  }

  @JsonIgnore
  public boolean isFree() {
    return this.domainId == null;
  }

  @JsonIgnore
  public void setFree() {
    this.domainId = null;
  }

  public Long getDomainId() {
    return domainId;
  }

  public void setDomainId(Long domainId) {
    this.domainId = domainId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((domainId == null) ? 0 : domainId.hashCode());
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
    InstanceDomain other = (InstanceDomain) obj;
    if (domainId == null) {
      if (other.domainId != null) {
        return false;
      }
    } else if (!domainId.equals(other.domainId)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "DomainId [domainId=" + domainId + "]";
  }

}
