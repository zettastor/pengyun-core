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

public enum StoragePoolStrategy {
  Capacity(1), Performance(2), Mixed(3);

  private final int value;

  private StoragePoolStrategy(int value) {
    this.value = value;
  }

  public static StoragePoolStrategy findByValue(int value) {
    switch (value) {
      case 1:
        return Capacity;
      case 2:
        return Performance;
      case 3:
        return Mixed;
      default:
        return null;
    }
  }

  public static StoragePoolStrategy findByName(String name) {
    if (name == null) {
      return null;
    }

    if (name.equals("Capacity")) {
      return Capacity;
    } else if (name.equals("Performance")) {
      return Performance;
    } else if (name.equals("Mixed")) {
      return Mixed;
    } else {
      return null;
    }
  }

  public int getValue() {
    return value;
  }

}
