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

public enum OperationName {
  Equipment {
    @Override
    public int getEvnetDataGeneratingPeriod() {
      return EventDataGeneratingPeriod.OneSecond.getValue();
    }
  },
  Volume {
    @Override
    public int getEvnetDataGeneratingPeriod() {
      return EventDataGeneratingPeriod.OneSecond.getValue();
    }
  },
  Container {
    @Override
    public int getEvnetDataGeneratingPeriod() {
      return EventDataGeneratingPeriod.OneSecond.getValue();
    }
  },
  StoragePool {
    @Override
    public int getEvnetDataGeneratingPeriod() {
      return EventDataGeneratingPeriod.FiveSecond.getValue();
    }
  },
  NETCARD {
    @Override
    public int getEvnetDataGeneratingPeriod() {
      return EventDataGeneratingPeriod.TenSecond.getValue();
    }
  },
  NETWORK {
    @Override
    public int getEvnetDataGeneratingPeriod() {
      return EventDataGeneratingPeriod.OneMinute.getValue();
    }
  },
  SERVICE {
    @Override
    public int getEvnetDataGeneratingPeriod() {
      return EventDataGeneratingPeriod.ZeroSecond.getValue();
    }
  },
  Disk_Space {
    @Override
    public int getEvnetDataGeneratingPeriod() {
      return EventDataGeneratingPeriod.TenSecond.getValue();
    }
  },
  Disk {
    @Override
    public int getEvnetDataGeneratingPeriod() {
      return EventDataGeneratingPeriod.TenSecond.getValue();
    }
  },
  DiskPerformance {
    @Override
    public int getEvnetDataGeneratingPeriod() {
      return EventDataGeneratingPeriod.OneSecond.getValue();
    }
  },
  DatanodeIo {
    @Override
    public int getEvnetDataGeneratingPeriod() {
      return EventDataGeneratingPeriod.FiveSecond.getValue();
    }
  },
  DataNode {
  },
  ServerNode {
    @Override
    public int getEvnetDataGeneratingPeriod() {
      return EventDataGeneratingPeriod.ZeroSecond.getValue();
    }
  },
  SYSTEM {
    @Override
    public int getEvnetDataGeneratingPeriod() {
      return EventDataGeneratingPeriod.ZeroSecond.getValue();
    }
  },
  NETWORK_STATUS {
    @Override
    public int getEvnetDataGeneratingPeriod() {
      return EventDataGeneratingPeriod.ZeroSecond.getValue();
    }
  },
  Driver {
    @Override
    public int getEvnetDataGeneratingPeriod() {
      return EventDataGeneratingPeriod.OneSecond.getValue();
    }
  },
  CsiVolume {
    @Override
    public int getEvnetDataGeneratingPeriod() {
      return EventDataGeneratingPeriod.OneSecond.getValue();
    }
  },
  CsiServer {
    @Override
    public int getEvnetDataGeneratingPeriod() {
      return EventDataGeneratingPeriod.ZeroSecond.getValue();
    }
  },
  PYD {
  },
  ISCSI {
  };

  public int getEvnetDataGeneratingPeriod() {
    return EventDataGeneratingPeriod.OneSecond.getValue();
  }

  ;

  enum EventDataGeneratingPeriod {
    ZeroSecond(0),
    OneSecond(1000 * 1),
    FiveSecond(1000 * 5),
    TenSecond(1000 * 10),
    OneMinute(1000 * 60);

    private int value;

    EventDataGeneratingPeriod(int second) {
      this.value = second;
    }

    public int getValue() {
      return value;
    }
  }
}
