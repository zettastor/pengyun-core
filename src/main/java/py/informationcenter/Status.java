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

public enum Status {
  Available(1), Deleting(2);

  private int value;

  private Status(int value) {
    this.value = value;
  }

  public static Status findByValue(int value) {
    switch (value) {
      case 1:
        return Available;
      case 2:
        return Deleting;
      default:
        return null;
    }
  }

  public static Status findByName(String name) {
    if (name == null) {
      return null;
    }

    if (name.equals("Available")) {
      return Available;
    } else if (name.equals("Deleting")) {
      return Deleting;
    } else {
      return null;
    }
  }

  public int getValue() {
    return value;
  }

}
