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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.AtomicDouble;
import java.sql.Blob;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoragePool {
  public static final Long DEFAULT_MIGRATION_RULE_ID = -1345L;
  public static final Double STORAGE_POOL_STABLE_RATIO = 100.00;
  private static final Logger logger = LoggerFactory.getLogger(StoragePool.class);
  private Long poolId;
  private Long domainId;
  private String domainName;
  private String name;
  private StoragePoolStrategy strategy;
  private String description;

  private Multimap<Long, Long> archivesInDataNode;
  private Set<Long> volumeIds;
  private AtomicLong lastUpdateTime;
  private Status status;
  private AtomicDouble usedRatio = new AtomicDouble();

  private long migrationRuleId = DEFAULT_MIGRATION_RULE_ID;

  private Long migrationSpeed = 0L;
  private Double migrationRatio = 100.0;

  private long totalSpace;
  private long freeSpace;
  private long logicalPssaaFreeSpace;
  private long logicalPssFreeSpace;
  private long logicalPsaFreeSpace;
  private String storagePoolLevel;

  private long totalMigrateDataSizeMb = 0;

  public StoragePool() {
    archivesInDataNode = Multimaps.synchronizedSetMultimap(HashMultimap.<Long, Long>create());
    volumeIds = new HashSet<>();
    this.status = Status.Available;
    this.lastUpdateTime = new AtomicLong();

  }

  public StoragePool(Long poolId, Long domainId, String name, StoragePoolStrategy strategy,
      String description) {
    archivesInDataNode = Multimaps.synchronizedSetMultimap(HashMultimap.<Long, Long>create());
    volumeIds = new HashSet<>();
    this.poolId = poolId;
    this.domainId = domainId;
    this.name = name;
    this.strategy = strategy;
    this.description = description;
    this.status = Status.Available;
    this.lastUpdateTime = new AtomicLong();

  }

  public boolean removeArchiveFromDatanode(Long datanodeId, Long archiveId) {
    return archivesInDataNode.remove(datanodeId, archiveId);
  }

  public String getDomainName() {
    return domainName;
  }

  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }

  public String getStoragePoolLevel() {
    return storagePoolLevel;
  }

  public void setStoragePoolLevel(String storagePoolLevel) {
    this.storagePoolLevel = storagePoolLevel;
  }

  public void addArchiveInDatanode(Long datanodeId, Long archiveId) {
    archivesInDataNode.put(datanodeId, archiveId);
  }

  public void addVolumeId(Long volumeId) {
    volumeIds.add(volumeId);
  }

  public void removeVolumeId(Long volumeId) {
    volumeIds.remove(volumeId);
  }

  public boolean timePassedLongEnough(Long passedTimeMs) {
    return System.currentTimeMillis() - getLastUpdateTime() > passedTimeMs;
  }

  public boolean isDeleting() {
    return status == Status.Deleting;
  }

  public StoragePoolInformationDb toStoragePoolInformationDb(
      StoragePoolDbStore storagePoolDbStore) {
    StoragePoolInformationDb storagePoolInformation = new StoragePoolInformationDb();

    Validate.notNull(poolId, "poolId can not be null");
    storagePoolInformation.setPoolId(poolId);

    Validate.notNull(domainId, "domainId can not be null");
    storagePoolInformation.setDomainId(domainId);

    Validate.notNull(name, "name can not be null");
    storagePoolInformation.setPoolName(name);

    Validate.notNull(strategy, "strategy can not be null");
    storagePoolInformation.setPoolStrategy(strategy.name());

    Validate.notNull(status, "status can not be null");
    storagePoolInformation.setStatus(status.name());

    Validate.notNull(lastUpdateTime, "lastUpdateTime can not be null");
    storagePoolInformation.setLastUpdateTime(getLastUpdateTime());

    if (description != null) {
      storagePoolInformation.setPoolDescription(description);
    }

    if (archivesInDataNode != null && !archivesInDataNode.isEmpty()) {
      String archivesInDataNodeStr = Utils.bulidStringFromMultiMap(archivesInDataNode);
      Blob archivesInDataNodeBlob = storagePoolDbStore.createBlob(archivesInDataNodeStr.getBytes());
      storagePoolInformation.setArchivesInDatanode(archivesInDataNodeBlob);
    }

    if (volumeIds != null && !volumeIds.isEmpty()) {
      String volumeIdsStr = Utils.bulidJsonStrFromObjectLong(volumeIds);
      Blob volumeIdsStrBlob = storagePoolDbStore.createBlob(volumeIdsStr.getBytes());
      storagePoolInformation.setVolumeIds(volumeIdsStrBlob);
    }

    storagePoolInformation.setMigrationRuleId(migrationRuleId);

    Validate.notNull(storagePoolLevel, "storage pool level can not be null");
    storagePoolInformation.setStoragePoolLevel(storagePoolLevel);

    Validate.notNull(domainName, "domain name can not be null");
    storagePoolInformation.setDomainName(domainName);

    return storagePoolInformation;
  }

  public StoragePoolInformation toStoragePoolInformation() {
    StoragePoolInformation storagePoolInformation = new StoragePoolInformation();

    Validate.notNull(poolId, "poolId can not be null");
    storagePoolInformation.setPoolId(poolId);

    Validate.notNull(domainId, "domainId can not be null");
    storagePoolInformation.setDomainId(domainId);

    Validate.notNull(name, "name can not be null");
    storagePoolInformation.setPoolName(name);

    Validate.notNull(strategy, "strategy can not be null");
    storagePoolInformation.setPoolStrategy(strategy.name());

    Validate.notNull(status, "status can not be null");
    storagePoolInformation.setStatus(status.name());

    Validate.notNull(lastUpdateTime, "lastUpdateTime can not be null");
    storagePoolInformation.setLastUpdateTime(getLastUpdateTime());

    if (description != null) {
      storagePoolInformation.setPoolDescription(description);
    }

    if (archivesInDataNode != null && !archivesInDataNode.isEmpty()) {
      storagePoolInformation
          .setArchivesInDatanode(Utils.bulidStringFromMultiMap(archivesInDataNode));
    }

    if (volumeIds != null && !volumeIds.isEmpty()) {
      storagePoolInformation.setVolumeIds(Utils.bulidJsonStrFromObjectLong(volumeIds));
    }

    storagePoolInformation.setMigrationRuleId(migrationRuleId);

    Validate.notNull(storagePoolLevel, "storage pool level can not be null");
    storagePoolInformation.setStoragePoolLevel(storagePoolLevel);

    Validate.notNull(domainName, "domain name can not be null");
    storagePoolInformation.setDomainName(domainName);
    return storagePoolInformation;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getPoolId() {
    return poolId;
  }

  public void setPoolId(Long poolId) {
    this.poolId = poolId;
  }

  public StoragePoolStrategy getStrategy() {
    return strategy;
  }

  public void setStrategy(StoragePoolStrategy strategy) {
    this.strategy = strategy;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Multimap<Long, Long> getArchivesInDataNode() {
    return archivesInDataNode;
  }

  public void setArchivesInDataNode(Multimap<Long, Long> archivesInDataNode) {
    this.archivesInDataNode = archivesInDataNode;
  }

  public Set<Long> getVolumeIds() {
    return volumeIds;
  }

  public void setVolumeIds(Set<Long> volumeIds) {
    this.volumeIds = volumeIds;
  }

  public Long getDomainId() {
    return domainId;
  }

  public void setDomainId(Long domainId) {
    this.domainId = domainId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StoragePool that = (StoragePool) o;

    if (totalSpace != that.totalSpace) {
      return false;
    }
    if (freeSpace != that.freeSpace) {
      return false;
    }
    if (logicalPssFreeSpace != that.logicalPssFreeSpace) {
      return false;
    }
    if (logicalPsaFreeSpace != that.logicalPsaFreeSpace) {
      return false;
    }
    if (poolId != null ? !poolId.equals(that.poolId) : that.poolId != null) {
      return false;
    }
    if (domainId != null ? !domainId.equals(that.domainId) : that.domainId != null) {
      return false;
    }
    if (domainName != null ? !domainName.equals(that.domainName) : that.domainName != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (strategy != that.strategy) {
      return false;
    }
    if (description != null ? !description.equals(that.description) : that.description != null) {
      return false;
    }
    if (archivesInDataNode != null ? !archivesInDataNode.equals(that.archivesInDataNode)
        : that.archivesInDataNode != null) {
      return false;
    }
    if (volumeIds != null ? !volumeIds.equals(that.volumeIds) : that.volumeIds != null) {
      return false;
    }
    if (lastUpdateTime != null ? lastUpdateTime.get() != that.lastUpdateTime.get()
        : that.lastUpdateTime != null) {
      return false;
    }
    if (status != that.status) {
      return false;
    }
    if (usedRatio != null ? usedRatio.get() != that.usedRatio.get() : that.usedRatio != null) {
      return false;
    }
    if (migrationSpeed != null ? !migrationSpeed.equals(that.migrationSpeed)
        : that.migrationSpeed != null) {
      return false;
    }
    if (migrationRatio != null ? !migrationRatio.equals(that.migrationRatio)
        : that.migrationRatio != null) {
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
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (strategy != null ? strategy.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (archivesInDataNode != null ? archivesInDataNode.hashCode() : 0);
    result = 31 * result + (volumeIds != null ? volumeIds.hashCode() : 0);
    result = 31 * result + (lastUpdateTime != null ? lastUpdateTime.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (usedRatio != null ? usedRatio.hashCode() : 0);
    result = 31 * result + (migrationSpeed != null ? migrationSpeed.hashCode() : 0);
    result = 31 * result + (migrationRatio != null ? migrationRatio.hashCode() : 0);
    result = 31 * result + (int) (totalSpace ^ (totalSpace >>> 32));
    result = 31 * result + (int) (freeSpace ^ (freeSpace >>> 32));
    result = 31 * result + (int) (logicalPssFreeSpace ^ (logicalPssFreeSpace >>> 32));
    result = 31 * result + (int) (logicalPsaFreeSpace ^ (logicalPsaFreeSpace >>> 32));
    result = 31 * result + (storagePoolLevel != null ? storagePoolLevel.hashCode() : 0);
    return result;
  }

  public Long getLastUpdateTime() {
    return lastUpdateTime.get();
  }

  public void setLastUpdateTime(Long lastUpdateTime) {
    this.lastUpdateTime.set(lastUpdateTime);
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Double getUsedRatio() {
    return usedRatio.get();
  }

  public void setUsedRatio(Double usedRatio) {
    this.usedRatio.set(usedRatio);
  }

  public long getMigrationRuleId() {
    return migrationRuleId;
  }

  public void setMigrationRuleId(long migrationRuleId) {
    this.migrationRuleId = migrationRuleId;
  }

  public Long getMigrationSpeed() {
    return migrationSpeed;
  }

  public void setMigrationSpeed(Long migrationSpeed) {
    this.migrationSpeed = migrationSpeed;
  }

  public Double getMigrationRatio() {
    return migrationRatio;
  }

  public void setMigrationRatio(Double migrationRatio) {
    this.migrationRatio = migrationRatio;
  }

  public long getLogicalPssaaFreeSpace() {
    return logicalPssaaFreeSpace;
  }

  public void setLogicalPssaaFreeSpace(long logicalPssaaFreeSpace) {
    this.logicalPssaaFreeSpace = logicalPssaaFreeSpace;
  }

  public long getLogicalPssFreeSpace() {
    return logicalPssFreeSpace;
  }

  public void setLogicalPssFreeSpace(long logicalPssFreeSpace) {
    this.logicalPssFreeSpace = logicalPssFreeSpace;
  }

  public long getLogicalPsaFreeSpace() {
    return logicalPsaFreeSpace;
  }

  public void setLogicalPsaFreeSpace(long logicalPsaFreeSpace) {
    this.logicalPsaFreeSpace = logicalPsaFreeSpace;
  }

  public long getTotalSpace() {
    return totalSpace;
  }

  public void setTotalSpace(long totalSpace) {
    this.totalSpace = totalSpace;
  }

  public long getFreeSpace() {
    return freeSpace;
  }

  public void setFreeSpace(long freeSpace) {
    this.freeSpace = freeSpace;
  }

  public long getTotalMigrateDataSizeMb() {
    return totalMigrateDataSizeMb;
  }

  public void setTotalMigrateDataSizeMb(long totalMigrateDataSizeMb) {
    this.totalMigrateDataSizeMb = totalMigrateDataSizeMb;
  }

  @Override
  public String toString() {
    return "StoragePool{" + "poolId=" + poolId + ", domainId=" + domainId + ", domainName='"
        + domainName + '\''
        + ", name='" + name + '\'' + ", strategy=" + strategy + ", description='" + description
        + '\''
        + ", archivesInDataNode=" + archivesInDataNode + ", volumeIds=" + volumeIds
        + ", lastUpdateTime="
        + lastUpdateTime + ", status=" + status + ", usedRatio=" + usedRatio
        + ", migrationRuleId=" + migrationRuleId + ", migrationSpeed="
        + migrationSpeed
        + ", migrationRatio=" + migrationRatio + ", totalSpace=" + totalSpace + ", freeSpace="
        + freeSpace
        + ", logicalPSSFreeSpace=" + logicalPssFreeSpace + ", logicalPSAFreeSpace="
        + logicalPsaFreeSpace
        + ", storagePoolLevel='" + storagePoolLevel + '\'' + '}';
  }
}
