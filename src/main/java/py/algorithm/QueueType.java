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

package py.algorithm;

import org.apache.commons.lang3.Validate;

public enum QueueType {
  T1(0),

  T2(1),

  B1(2),

  B2(3),

  LRU(4),
  ;

  private static QueueType[] array = new QueueType[]{T1, T2, B1, B2, LRU};

  static {
    Validate.isTrue(T1 == array[T1.index]);
    Validate.isTrue(T2 == array[T2.index]);
    Validate.isTrue(B1 == array[B1.index]);
    Validate.isTrue(B2 == array[B2.index]);
    Validate.isTrue(LRU == array[LRU.index]);
  }

  private final int index;

  QueueType(int index) {
    this.index = index;
  }

  public static QueueType valueOf(int index) {
    return array[index];
  }

  public int index() {
    return index;
  }

  public boolean isTxQueue() {
    return this == T1 || this == T2;
  }

  public boolean isBxQueue() {
    return this == B1 || this == B2;
  }
}
