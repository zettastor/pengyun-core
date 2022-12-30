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

package py.informationcenter;

import com.google.common.collect.Multimap;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.Validate;

public class StoragePoolInformation {
  private Long poolId;
  private Long domainId;
  private String domainName;
  private String poolName;
  private String poolStrategy;
  private String poolDescription;
  private String archivesInDatanode;
  private String volumeIds;
  private String status;
  private Long lastUpdateTime;
  private String storagePoolLevel;
  private long migrationRuleId;

  public StoragePoolInformation() {
  }

  public Long getPoolId() {
    return poolId;
  }

  public void setPoolId(Long poolId) {
    this.poolId = poolId;
  }

  public Long getDomainId() {
    return domainId;
  }

  public void setDomainId(Long domainId) {
    this.domainId = domainId;
  }

  public String getPoolName() {
    return poolName;
  }

  public void setPoolName(String poolName) {
    this.poolName = poolName;
  }

  public String getPoolStrategy() {
    return poolStrategy;
  }

  public void setPoolStrategy(String poolStrategy) {
    this.poolStrategy = poolStrategy;
  }

  public String getPoolDescription() {
    return poolDescription;
  }

  public void setPoolDescription(String poolDescription) {
    this.poolDescription = poolDescription;
  }

  public String getArchivesInDatanode() {
    return archivesInDatanode;
  }

  public void setArchivesInDatanode(String archivesInDatanode) {
    this.archivesInDatanode = archivesInDatanode;
  }

  public String getVolumeIds() {
    return volumeIds;
  }

  public void setVolumeIds(String volumeIds) {
    this.volumeIds = volumeIds;
  }

  public String getStoragePoolLevel() {
    return storagePoolLevel;
  }

  public void setStoragePoolLevel(String storagePoolLevel) {
    this.storagePoolLevel = storagePoolLevel;
  }

  public String getDomainName() {
    return domainName;
  }

  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }

  public StoragePool toStoragePool() {
    StoragePool storagePool = new StoragePool();
    Validate.notNull(domainId);
    storagePool.setDomainId(domainId);

    Validate.notNull(poolId);
    storagePool.setPoolId(poolId);

    Validate.notNull(poolName);
    storagePool.setName(poolName);

    Validate.notNull(poolStrategy);
    storagePool.setStrategy(StoragePoolStrategy.findByName(poolStrategy));

    Validate.notNull(lastUpdateTime);
    storagePool.setLastUpdateTime(lastUpdateTime);

    if (poolDescription != null) {
      storagePool.setDescription(poolDescription);
    }

    if (archivesInDatanode != null) {
      Multimap<Long, Long> multiMap = Utils.parseObjecOfMultiMapFromJsonStr(archivesInDatanode);
      Validate.notEmpty(multiMap.asMap());
      storagePool.setArchivesInDataNode(multiMap);
    }

    if (volumeIds != null) {
      Set<Long> volumeList = new HashSet<Long>();
      volumeList.addAll(Utils.parseObjecLongFromJsonStr(volumeIds));
      Validate.notNull(volumeList);
      storagePool.setVolumeIds(volumeList);
    }

    storagePool.setMigrationRuleId(migrationRuleId);

    Validate.notNull(storagePoolLevel, "storage pool level can not be null");
    storagePool.setStoragePoolLevel(storagePoolLevel);

    Validate.notNull(domainName, "domain name can not be null");
    storagePool.setDomainName(domainName);
    return storagePool;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StoragePoolInformation that = (StoragePoolInformation) o;

    if (poolId != null ? !poolId.equals(that.poolId) : that.poolId != null) {
      return false;
    }
    if (domainId != null ? !domainId.equals(that.domainId) : that.domainId != null) {
      return false;
    }
    if (domainName != null ? !domainName.equals(that.domainName) : that.domainName != null) {
      return false;
    }
    if (poolName != null ? !poolName.equals(that.poolName) : that.poolName != null) {
      return false;
    }
    if (poolStrategy != null ? !poolStrategy.equals(that.poolStrategy)
        : that.poolStrategy != null) {
      return false;
    }
    if (poolDescription != null ? !poolDescription.equals(that.poolDescription)
        : that.poolDescription != null) {
      return false;
    }
    if (archivesInDatanode != null ? !archivesInDatanode.equals(that.archivesInDatanode)
        : that.archivesInDatanode != null) {
      return false;
    }
    if (volumeIds != null ? !volumeIds.equals(that.volumeIds) : that.volumeIds != null) {
      return false;
    }
    if (status != null ? !status.equals(that.status) : that.status != null) {
      return false;
    }
    if (lastUpdateTime != null ? !lastUpdateTime.equals(that.lastUpdateTime)
        : that.lastUpdateTime != null) {
      return false;
    }
    return storagePoolLevel != null ? storagePoolLevel.equals(that.storagePoolLevel)
        : that.storagePoolLevel == null;
  }

  @Override
  public int hashCode() {
    int result = poolId != null ? poolId.hashCode() : 0;
    result = 31 * result + (domainId != null ? domainId.hashCode() : 0);
    result = 31 * result + (domainName != null ? domainName.hashCode() : 0);
    result = 31 * result + (poolName != null ? poolName.hashCode() : 0);
    result = 31 * result + (poolStrategy != null ? poolStrategy.hashCode() : 0);
    result = 31 * result + (poolDescription != null ? poolDescription.hashCode() : 0);
    result = 31 * result + (archivesInDatanode != null ? archivesInDatanode.hashCode() : 0);
    result = 31 * result + (volumeIds != null ? volumeIds.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (lastUpdateTime != null ? lastUpdateTime.hashCode() : 0);
    result = 31 * result + (storagePoolLevel != null ? storagePoolLevel.hashCode() : 0);
    return result;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(Long lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  public long getMigrationRuleId() {
    return migrationRuleId;
  }

  public void setMigrationRuleId(long migrationRuleId) {
    this.migrationRuleId = migrationRuleId;
  }

  @Override
  public String toString() {
    return "StoragePoolInformation{" + "poolId=" + poolId + ", domainId=" + domainId
        + ", domainName='" + domainName
        + '\'' + ", poolName='" + poolName + '\'' + ", poolStrategy='" + poolStrategy + '\''
        + ", poolDescription='" + poolDescription + '\'' + ", archivesInDatanode='"
        + archivesInDatanode + '\''
        + ", volumeIds='" + volumeIds + '\'' + ", status='" + status + '\'' + ", lastUpdateTime="
        + lastUpdateTime + ", storagePoolLevel='" + storagePoolLevel + '\'' + ", migrationRuleId="
        + migrationRuleId + '}';
  }
}
