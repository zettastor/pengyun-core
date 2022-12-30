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

public enum DiskInfoLightStatus {
  OFF(0),
  ON(1),
  UNKNOWN(2);

  private int value;

  DiskInfoLightStatus(int value) {
    this.value = value;
  }

  public static DiskInfoLightStatus getFromValue(int value) {
    for (DiskInfoLightStatus lightStatus : DiskInfoLightStatus.values()) {
      if (lightStatus.value == value) {
        return lightStatus;
      }
    }
    return UNKNOWN;
  }

  public int getValue() {
    return value;
  }
}
