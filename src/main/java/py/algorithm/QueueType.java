/**
* Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
