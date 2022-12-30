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

package py.common;

public class OffsetAndLength {
  public static final int INVALID_OFFSET = -1;
  private final int offset;
  private final int length;

  public OffsetAndLength(int offset, int length) {
    this.offset = offset;
    this.length = length;
  }

  public boolean legal() {
    return (offset == INVALID_OFFSET) ? false : true;
  }

  public int getOffset() {
    return offset;
  }

  public int getLength() {
    return length;
  }

}