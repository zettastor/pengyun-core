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

public enum MonitorObjectEnum {
  NODE("节点"),
  DISK("磁盘"),
  NETWORK_CARD("网卡"),
  VOLUME("卷"),
  CONTAINER("容器"),
  STORAGE_POOL("存储池"),
  NETWORK("网络"),
  SERVICE("服务"),
  DRIVER("驱动"),
  SYSTEM("系统");

  private String cnName;

  MonitorObjectEnum(String cnName) {
    this.cnName = cnName;
  }

  public String getCnName() {
    return cnName;
  }
}
