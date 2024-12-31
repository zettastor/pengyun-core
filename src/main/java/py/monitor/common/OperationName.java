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
