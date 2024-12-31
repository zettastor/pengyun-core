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
