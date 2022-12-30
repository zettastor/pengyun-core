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

package py.monitor.common;

public enum CounterName {
  CPU {
    @Override
    public String getCounterNameCn() {
      return "CPU使用率";
    }

    @Override
    public String getCounterNameEn() {
      return "CPU use rate";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.NODE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Equipment;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  MEMORY {
    @Override
    public String getCounterNameCn() {
      return "内存使用率";
    }

    @Override
    public String getCounterNameEn() {
      return "Memory";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.NODE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Equipment;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  DISK_STATUS {
    @Override
    public String getCounterNameCn() {
      return "磁盘状态";
    }

    @Override
    public String getCounterNameEn() {
      return "Disk_status";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.DISK;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Disk;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }
  },

  NETWORK_STATUS {
    @Override
    public String getCounterNameCn() {
      return "网卡状态";
    }

    @Override
    public String getCounterNameEn() {
      return "Netcard status";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.NETWORK_CARD;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.NETWORK_STATUS;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }
  },

  SERVICE_STATUS {
    @Override
    public String getCounterNameCn() {
      return "服务状态";
    }

    @Override
    public String getCounterNameEn() {
      return "Service status";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.SERVICE;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }
  },

  CSI_SERVICE_STATUS {
    @Override
    public String getCounterNameCn() {
      return "Csi服务状态";
    }

    @Override
    public String getCounterNameEn() {
      return "Csi Service status";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.SERVICE;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }
  },
  SERVICE_STATUS_ZOOKEEPER {
    @Override
    public String getCounterNameCn() {
      return "zookeeper服务状态";
    }

    @Override
    public String getCounterNameEn() {
      return "zookeeper Service status";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.SERVICE;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }
  },
  SERVERNODE_STATUS {
    @Override
    public String getCounterNameCn() {
      return "服务器节点状态";
    }

    @Override
    public String getCounterNameEn() {
      return "Server node status";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.NODE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.ServerNode;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }
  },

  DRIVER_STATUS {
    @Override
    public String getCounterNameCn() {
      return "驱动状态";
    }

    @Override
    public String getCounterNameEn() {
      return "Driver status";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.DRIVER;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Driver;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }
  },

  DRIVER_CSI_NUMBER {
    @Override
    public String getCounterNameCn() {
      return "csi卷驱动数量大于2";
    }

    @Override
    public String getCounterNameEn() {
      return "Csi driver number lager the 2";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.DRIVER;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Driver;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }
  },

  DRIVER_CSI_OK_NUMBER {
    @Override
    public String getCounterNameCn() {
      return "csi卷正常驱动数量小于2";
    }

    @Override
    public String getCounterNameEn() {
      return "csi ok driver number is small 2";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.DRIVER;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Driver;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }
  },

  DRIVER_CSI_CONNECT {
    @Override
    public String getCounterNameCn() {
      return "csi卷驱动用户连接数量小于2";
    }

    @Override
    public String getCounterNameEn() {
      return "csi driver connect number is samll 2";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.DRIVER;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Driver;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }
  },

  VOLUMEDELETE_DELAY {
    @Override
    public String getCounterNameCn() {
      return "卷延迟删除";
    }

    @Override
    public String getCounterNameEn() {
      return "volume delete delay info";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.VOLUME;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Volume;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }
  },

  DATANODE_IO_DELAY {
    @Override
    public String getCounterNameCn() {
      return "DataNode IO 延迟";
    }

    @Override
    public String getCounterNameEn() {
      return "DataNode IO delay";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.DatanodeIo;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  VOLUME_READ_IOPS {
    @Override
    public String getCounterNameCn() {
      return "卷IOPS读";
    }

    @Override
    public String getCounterNameEn() {
      return "Volume read iops";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.VOLUME;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Volume;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  VOLUME_WRITE_IOPS {
    @Override
    public String getCounterNameCn() {
      return "卷IOPS写";
    }

    @Override
    public String getCounterNameEn() {
      return "Volume write iops";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.VOLUME;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Volume;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  VOLUME_READ_THROUGHPUT {
    @Override
    public String getCounterNameCn() {
      return "卷吞吐量读";
    }

    @Override
    public String getCounterNameEn() {
      return "Volume read throughput";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.VOLUME;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Volume;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  VOLUME_WRITE_THROUGHPUT {
    @Override
    public String getCounterNameCn() {
      return "卷吞吐量写";
    }

    @Override
    public String getCounterNameEn() {
      return "Volume write throughput";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.VOLUME;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Volume;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  VOLUME_READ_LATENCY {
    @Override
    public String getCounterNameCn() {
      return "卷读延迟";
    }

    @Override
    public String getCounterNameEn() {
      return "Volume read latency";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.VOLUME;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Volume;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  VOLUME_WRITE_LATENCY {
    @Override
    public String getCounterNameCn() {
      return "卷写延迟";
    }

    @Override
    public String getCounterNameEn() {
      return "Volume write latency";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.VOLUME;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Volume;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  VOLUME_IO_BLOCK_SIZE {
    @Override
    public String getCounterNameCn() {
      return "卷IO块大小";
    }

    @Override
    public String getCounterNameEn() {
      return "Volume io block size";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.VOLUME;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Volume;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return false;
    }
  },

  VOLUME_CSI_FREE_SPACE {
    @Override
    public String getCounterNameCn() {
      return "csi卷的剩余空间";
    }

    @Override
    public String getCounterNameEn() {
      return "Csi volume free space";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.VOLUME;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.CsiVolume;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  VOLUME_CSI_READ_ONLY {
    @Override
    public String getCounterNameCn() {
      return "csi卷 read only";
    }

    @Override
    public String getCounterNameEn() {
      return "csi volume read only";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.VOLUME;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.CsiVolume;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  STORAGEPOOL_GROUP_AMOUNT {
    @Override
    public String getCounterNameCn() {
      return "存储池不可用";
    }

    @Override
    public String getCounterNameEn() {
      return "StoragePool group amount";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.STORAGE_POOL;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.StoragePool;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  STORAGEPOOL_FREE_SPACE_RATIO {
    @Override
    public String getCounterNameCn() {
      return "存储池剩余空间百分比";
    }

    @Override
    public String getCounterNameEn() {
      return "StoragePool free space ratio";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.STORAGE_POOL;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.StoragePool;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  STORAGEPOOL_AVAILABLE_PSS_SEGMENT_COUNT {
    @Override
    public String getCounterNameCn() {
      return "存储池可用的PSS segment个数";
    }

    @Override
    public String getCounterNameEn() {
      return "StoragePool available PSS segment count";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.STORAGE_POOL;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.StoragePool;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  STORAGEPOOL_AVAILABLE_PSA_SEGMENT_COUNT {
    @Override
    public String getCounterNameCn() {
      return "存储池可用的PSA segment个数";
    }

    @Override
    public String getCounterNameEn() {
      return "StoragePool available PSA segment count";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.STORAGE_POOL;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.StoragePool;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  STORAGEPOOL_VOLUME_AMOUNT {
    @Override
    public String getCounterNameCn() {
      return "存储池卷数量";
    }

    @Override
    public String getCounterNameEn() {
      return "StoragePool volume amount";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.STORAGE_POOL;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.StoragePool;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  STORAGEPOOL_LOST_DISK {
    @Override
    public String getCounterNameCn() {
      return "存储池磁盘丢失";
    }

    @Override
    public String getCounterNameEn() {
      return "StoragePool lost disk";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.STORAGE_POOL;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.StoragePool;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  STORAGEPOOL_REBUILD_FAIL {
    @Override
    public String getCounterNameCn() {
      return "存储池重构失败";
    }

    @Override
    public String getCounterNameEn() {
      return "StoragePool regular fail";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.STORAGE_POOL;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.StoragePool;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  STORAGEPOOL_READ_IOPS {
    @Override
    public String getCounterNameCn() {
      return "存储池IOPS读";
    }

    @Override
    public String getCounterNameEn() {
      return "StoragePool read iops";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.STORAGE_POOL;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.StoragePool;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  STORAGEPOOL_WRITE_IOPS {
    @Override
    public String getCounterNameCn() {
      return "存储池IOPS写";
    }

    @Override
    public String getCounterNameEn() {
      return "StoragePool write iops";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.STORAGE_POOL;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.StoragePool;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  STORAGEPOOL_READ_THROUGHPUT {
    @Override
    public String getCounterNameCn() {
      return "存储池吞吐量读";
    }

    @Override
    public String getCounterNameEn() {
      return "StoragePool read throughput";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.STORAGE_POOL;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.StoragePool;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  STORAGEPOOL_WRITE_THROUGHPUT {
    @Override
    public String getCounterNameCn() {
      return "存储池吞吐量写";
    }

    @Override
    public String getCounterNameEn() {
      return "StoragePool write throughput";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.STORAGE_POOL;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.StoragePool;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  STORAGEPOOL_READ_LATENCY {
    @Override
    public String getCounterNameCn() {
      return "存储池读延迟";
    }

    @Override
    public String getCounterNameEn() {
      return "StoragePool read latency";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.STORAGE_POOL;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.StoragePool;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  STORAGEPOOL_WRITE_LATENCY {
    @Override
    public String getCounterNameCn() {
      return "存储池写延迟";
    }

    @Override
    public String getCounterNameEn() {
      return "StoragePool write latency";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.STORAGE_POOL;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.StoragePool;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  STORAGEPOOL_IO_BLOCK_SIZE {
    @Override
    public String getCounterNameCn() {
      return "存储池IO块大小";
    }

    @Override
    public String getCounterNameEn() {
      return "StoragePool io block size";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.STORAGE_POOL;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.StoragePool;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return false;
    }
  },

  SYSTEM_READ_IOPS {
    @Override
    public String getCounterNameCn() {
      return "系统总IOPS读";
    }

    @Override
    public String getCounterNameEn() {
      return "System read iops";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SYSTEM;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.SYSTEM;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return false;
    }
  },

  SYSTEM_WRITE_IOPS {
    @Override
    public String getCounterNameCn() {
      return "系统总IOPS写";
    }

    @Override
    public String getCounterNameEn() {
      return "System write iops";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SYSTEM;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.SYSTEM;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return false;
    }
  },

  SYSTEM_READ_THROUGHPUT {
    @Override
    public String getCounterNameCn() {
      return "系统总吞吐量读";
    }

    @Override
    public String getCounterNameEn() {
      return "System read throughput";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SYSTEM;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.SYSTEM;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return false;
    }
  },

  SYSTEM_WRITE_THROUGHPUT {
    @Override
    public String getCounterNameCn() {
      return "系统总吞吐量写";
    }

    @Override
    public String getCounterNameEn() {
      return "System write throughput";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SYSTEM;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.SYSTEM;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return false;
    }
  },

  SYSTEM_READ_LATENCY {
    @Override
    public String getCounterNameCn() {
      return "系统总读延迟";
    }

    @Override
    public String getCounterNameEn() {
      return "System read latency";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SYSTEM;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.SYSTEM;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return false;
    }
  },

  SYSTEM_WRITE_LATENCY {
    @Override
    public String getCounterNameCn() {
      return "系统总写延迟";
    }

    @Override
    public String getCounterNameEn() {
      return "System write latency";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SYSTEM;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.SYSTEM;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return false;
    }
  },

  SYSTEM_IO_BLOCK_SIZE {
    @Override
    public String getCounterNameCn() {
      return "系统总IO块大小";
    }

    @Override
    public String getCounterNameEn() {
      return "System io block size";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SYSTEM;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.SYSTEM;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return false;
    }
  },

  NETCARD_DELAY {
    @Override
    public String getCounterNameCn() {
      return "网络延迟";
    }

    @Override
    public String getCounterNameEn() {
      return "Netcard delay";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.NETWORK;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.NETWORK;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  NETWORK_DROPPED {
    @Override
    public String getCounterNameCn() {
      return "网卡丢包率";
    }

    @Override
    public String getCounterNameEn() {
      return "Netcard dropped";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.NETWORK;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.NETWORK;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  SERVICE_HEARTBEAT {
    @Override
    public String getCounterNameCn() {
      return "服务心跳";
    }

    @Override
    public String getCounterNameEn() {
      return "Service heartbeat";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.SERVICE;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return false;
    }
  },

  TRACK_STATUS {
    @Override
    public String getCounterNameCn() {
      return "磁道状态";
    }

    @Override
    public String getCounterNameEn() {
      return "Track status";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.DISK;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Disk;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }
  },

  DISK_TEMPERATURE {
    @Override
    public String getCounterNameCn() {
      return "磁盘温度";
    }

    @Override
    public String getCounterNameEn() {
      return "Disk temperature";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.DISK;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Disk;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  DISK_READ_IOPS {
    @Override
    public String getCounterNameCn() {
      return "磁盘IOPS读";
    }

    @Override
    public String getCounterNameEn() {
      return "Disk read iops";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.DISK;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.DiskPerformance;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return false;
    }
  },

  DISK_FREE_SPACE_RATIO {
    @Override
    public String getCounterNameCn() {
      return "磁盘剩余容量百分比";
    }

    @Override
    public String getCounterNameEn() {
      return "Disk free space ratio";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.DISK;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.Disk_Space;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  DISK_WRITE_IOPS {
    @Override
    public String getCounterNameCn() {
      return "磁盘IOPS写";
    }

    @Override
    public String getCounterNameEn() {
      return "Disk write iops";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.DISK;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.DiskPerformance;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return false;
    }
  },

  DISK_READ_THROUGHPUT {
    @Override
    public String getCounterNameCn() {
      return "磁盘吞吐量读";
    }

    @Override
    public String getCounterNameEn() {
      return "Disk read throughput";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.DISK;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.DiskPerformance;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return false;
    }
  },

  DISK_WRITE_THROUGHPUT {
    @Override
    public String getCounterNameCn() {
      return "磁盘吞吐量写";
    }

    @Override
    public String getCounterNameEn() {
      return "Disk write throughput";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.DISK;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.DiskPerformance;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return false;
    }
  },

  DISK_READ_WAIT {
    @Override
    public String getCounterNameCn() {
      return "磁盘等待时间读";
    }

    @Override
    public String getCounterNameEn() {
      return "Disk read wait";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.DISK;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.DiskPerformance;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return false;
    }
  },

  DISK_WRITE_WAIT {
    @Override
    public String getCounterNameCn() {
      return "磁盘等待时间写";
    }

    @Override
    public String getCounterNameEn() {
      return "Disk write wait";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.DISK;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.DiskPerformance;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return false;
    }
  },

  DISK_UTIL {
    @Override
    public String getCounterNameCn() {
      return "磁盘IO利用率";
    }

    @Override
    public String getCounterNameEn() {
      return "Disk util";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.DISK;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.DiskPerformance;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return true;
    }

    @Override
    public boolean isAlertItem() {
      return false;
    }
  },

  PYD_QUEUE_IO_DELAY {
    @Override
    public String getCounterNameCn() {
      return "PYD查询IO延迟";
    }

    @Override
    public String getCounterNameEn() {
      return "PYD queue IO delay";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.PYD;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  PYD_IO_DELAY {
    @Override
    public String getCounterNameCn() {
      return "PYD IO延迟";
    }

    @Override
    public String getCounterNameEn() {
      return "PYD IO DELAY";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.PYD;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  PYD_IO_TIMEOUT {
    @Override
    public String getCounterNameCn() {
      return "PYD IO超时";
    }

    @Override
    public String getCounterNameEn() {
      return "PYD IO timeout";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.PYD;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  ISCSI_ABORT_TASK {
    @Override
    public String getCounterNameCn() {
      return "ISCSI任务中止";
    }

    @Override
    public String getCounterNameEn() {
      return "ISCSI task abort";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.ISCSI;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  ISCSI_ACCESS_LUN {
    @Override
    public String getCounterNameCn() {
      return "ISCSI无法访问LUN";
    }

    @Override
    public String getCounterNameEn() {
      return "ISCSI access LUN";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.ISCSI;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  ISCSI_LOGIN_TIMEOUT {
    @Override
    public String getCounterNameCn() {
      return "ISCSI登录超时";
    }

    @Override
    public String getCounterNameEn() {
      return "ISCSI login timeout";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.ISCSI;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  ISCSI_LOGIN_NEGOTIATION_FAILED {
    @Override
    public String getCounterNameCn() {
      return "ISCSI登录协商失败";
    }

    @Override
    public String getCounterNameEn() {
      return "ISCSI login negotiation failed";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.ISCSI;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  DATANODE_REQUEST_QUEUE_LENGTH {
    @Override
    public String getCounterNameCn() {
      return "DataNode请求队列长度";
    }

    @Override
    public String getCounterNameEn() {
      return "DataNode request queue length";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.DataNode;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  DATANODE_GC_TIME {
    @Override
    public String getCounterNameCn() {
      return "DataNode GC时间";
    }

    @Override
    public String getCounterNameEn() {
      return "DataNode GC time";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.DataNode;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },
  DATANODE_MEMORY_USAGE {
    @Override
    public String getCounterNameCn() {
      return "DataNode内存使用";
    }

    @Override
    public String getCounterNameEn() {
      return "DataNode memory usage";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.DataNode;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },
  SERVICE_SUB_TASK_COUNT {
    @Override
    public String getCounterNameCn() {
      return "服务子任务数量";
    }

    @Override
    public String getCounterNameEn() {
      return "Services task count";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.SERVICE;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },
  SERVICE_HANDLE_COUNT {
    @Override
    public String getCounterNameCn() {
      return "服务句柄数量";
    }

    @Override
    public String getCounterNameEn() {
      return "Services handle count";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.SERVICE;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Threshold;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },
  ERROR_LOG {
    @Override
    public String getCounterNameCn() {
      return "服务错误日志";
    }

    @Override
    public String getCounterNameEn() {
      return "Services error log";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.SERVICE;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.SERVICE;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },
  NET_SUB_HEALTH {
    @Override
    public String getCounterNameCn() {
      return "节点网络亚健康";
    }

    @Override
    public String getCounterNameEn() {
      return "Data node net sub-health";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.NETWORK;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.NETWORK;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  DATANODE_SEGREGATED {
    @Override
    public String getCounterNameCn() {
      return "存储节点被隔离";
    }

    @Override
    public String getCounterNameEn() {
      return "Storage node segregated";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.NETWORK;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.NETWORK;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  STORAGE_POOL_CANNOT_REBUILD {
    @Override
    public String getCounterNameCn() {
      return "存储池无法重构";
    }

    @Override
    public String getCounterNameEn() {
      return "Cannot rebuild storage pool";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.STORAGE_POOL;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.StoragePool;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  },

  NETWORK_CONGESTION {
    @Override
    public String getCounterNameCn() {
      return "网络拥塞";
    }

    @Override
    public String getCounterNameEn() {
      return "Network congestion";
    }

    @Override
    public MonitorObjectEnum getMonitorObjectEnum() {
      return MonitorObjectEnum.NETWORK;
    }

    @Override
    public OperationName getOperationName() {
      return OperationName.NETWORK;
    }

    @Override
    public CounterType getCounterType() {
      return CounterType.Status;
    }

    @Override
    public boolean isPerformanceItem() {
      return false;
    }

    @Override
    public boolean isAlertItem() {
      return true;
    }
  };

  public enum CounterType {
    Status,
    Threshold;
  }

  public abstract MonitorObjectEnum getMonitorObjectEnum();

  public abstract OperationName getOperationName();

  public abstract boolean isPerformanceItem();

  public abstract boolean isAlertItem();

  public abstract CounterType getCounterType();

  public abstract String getCounterNameCn();

  public abstract String getCounterNameEn();

  public static CounterName foundByName(String name) {
    for (CounterName counterName : values()) {
      if (counterName.name().equals(name)) {
        return counterName;
      }
    }
    return null;
  }
}