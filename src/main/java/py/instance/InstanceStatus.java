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

public enum InstanceStatus {
  HEALTHY(1),
  SUSPEND(2),
  SICK(3),
  FAILED(4),
  DISUSED(5);

  private final int value;

  private InstanceStatus(int value) {
    this.value = value;
  }

  public static InstanceStatus findByValue(int value) {
    for (InstanceStatus status : values()) {
      if (value == status.value) {
        return status;
      }
    }

    return null;
  }

  public int getValue() {
    return value;
  }

  public int compareWith(InstanceStatus other) {
    if (this.value > other.value) {
      return 1;
    } else if (this.value < other.value) {
      return -1;
    } else {
      return 0;
    }
  }
}
