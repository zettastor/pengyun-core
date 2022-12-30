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

package py.netty.core;

public enum IoEventThreadsMode {
  Fix_Threads_Mode(0),
  Calculate_From_Available_Core(1);

  int value;

  IoEventThreadsMode(int value) {
    this.value = value;
  }

  public static IoEventThreadsMode findByValue(int value) {
    switch (value) {
      case 0:
        return Fix_Threads_Mode;
      case 1:
        return Calculate_From_Available_Core;
      default:
        return null;
    }
  }

  public int getValue() {
    return value;
  }
}
