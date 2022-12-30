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

package py.transfer;

public enum PyIoMessageType {
  ReadData(1), WriteData(2), CrossReadData(3), CrossWriteData(4);

  private final int value;

  private PyIoMessageType(int value) {
    this.value = value;
  }

  public static PyIoMessageType findByValue(int value) {
    switch (value) {
      case 1:
        return ReadData;
      case 2:
        return WriteData;
      case 3:
        return CrossReadData;
      case 4:
        return CrossWriteData;
      default:
        return null;
    }
  }

  public int getValue() {
    return value;
  }
}
