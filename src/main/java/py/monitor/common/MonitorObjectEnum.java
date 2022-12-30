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
