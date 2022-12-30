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

package py.dd.common;

public enum ServicePort {
  InfoCenter(8020), DIH(10000), DriverContainer(9000), FsServer(8030), DataNode(10011), Console(
      8080), ScriptContainer(9090), MonitorCenter(11000), MonitorServer(11005), SystemDaemon(
      13333), RESTful(8081);

  private final int value;

  private ServicePort(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

}
