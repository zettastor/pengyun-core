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

public enum AccessPermissionType {
  READ(1) {
  },

  WRITE(2) {
    @Override
    public boolean canWrite() {
      return true;
    }
  },

  READWRITE(3) {
    @Override
    public boolean canWrite() {
      return true;
    }
  };

  private final int value;

  private AccessPermissionType(int value) {
    this.value = value;
  }

  public static AccessPermissionType findByValue(int value) {
    switch (value) {
      case 1:
        return READ;
      case 2:
        return WRITE;
      case 3:
        return READWRITE;
      default:
        return null;
    }
  }

  public int getValue() {
    return value;
  }

  public boolean canWrite() {
    return false;
  }
}
