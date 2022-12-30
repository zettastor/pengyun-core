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

package py.common.struct;

import java.util.concurrent.atomic.LongAdder;

public class PyRingBuffer<T> {
  public static final String ELEMENT_IDENTIFIER = "element";

  private final int limit;

  private LongAdder nextWriteIndex;

  private LongAdder headIndex;

  private Object[] elementData;

  public PyRingBuffer(int limit) {
    this.limit = limit;
    this.elementData = new Object[this.limit];
    this.nextWriteIndex = new LongAdder();
    this.nextWriteIndex.reset();
    this.headIndex = new LongAdder();
    this.headIndex.reset();
  }

  public void offer(Object dataValue) {
    long writeIndex = nextWriteIndex.longValue();

    elementData[(int) (writeIndex % limit)] = dataValue;
    nextWriteIndex.increment();

    if ((writeIndex + 1) > limit) {
      headIndex.increment();
    }
  }

  /**
   * this call will move read index forward, please make sure you only want read once.
   */
  public T next() {
    long readIndex = headIndex.longValue();
    if (readIndex >= nextWriteIndex.longValue()) {
      return null;
    }

    T t = (T) elementData[(int) (readIndex % limit)];
    headIndex.increment();
    return t;
  }

  /**
   * this call won't move read index.
   */
  public T getFirst() {
    T t = (T) elementData[(int) (headIndex.longValue() % limit)];
    return t;
  }

  /**
   * this call won't move read index.
   */
  public T getLast() {
    long gotNextWriteIndex = nextWriteIndex.longValue();
    if (gotNextWriteIndex == 0) {
      return null;
    }
    int readIndex = (int) ((gotNextWriteIndex - 1) % limit);
    T t = (T) elementData[readIndex];
    return t;
  }

  public int size() {
    long gotNextWriteIndex = nextWriteIndex.longValue();

    if (gotNextWriteIndex > limit) {
      return limit;
    } else {
      return (int) gotNextWriteIndex;
    }
  }

  public int getLimit() {
    return limit;
  }

  public boolean isFull() {
    return size() == limit;
  }

  public String buildString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("PYRingBuffer:\n");
    while (true) {
      T element = next();
      if (element == null) {
        break;
      } else {
        stringBuilder.append(ELEMENT_IDENTIFIER);
        stringBuilder.append(":[");
        stringBuilder.append(element);
        stringBuilder.append("],\n");
      }
    }
    return stringBuilder.toString();
  }

  public void release() {
    this.elementData = null;
  }

}
