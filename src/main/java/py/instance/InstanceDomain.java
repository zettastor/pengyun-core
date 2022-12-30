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
