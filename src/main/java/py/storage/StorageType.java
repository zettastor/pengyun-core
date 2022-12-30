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

package py.storage;

public enum StorageType {
  SATA(0), SSD(1), PCIE(2);

  private final int value;

  private StorageType(int value) {
    this.value = value;
  }

  public static StorageType findByValue(int value) {
    switch (value) {
      case 0:
        return SATA;
      case 1:
        return SSD;
      case 2:
        return PCIE;
      default:
        return null;
    }
  }

  public int getValue() {
    return value;
  }
}
