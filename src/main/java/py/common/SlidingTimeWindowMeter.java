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

package py.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SlidingTimeWindowMeter {
  // time unit span in MS
  private final long unitSpanMs;

  // elements queue, stored in an array
  private final Element[] elements;
  private volatile int currentIndex;

  public SlidingTimeWindowMeter(int size, long unitSpanMs) {
    this.unitSpanMs = unitSpanMs;
    this.elements = new Element[size];
    for (int i = 0; i < elements.length; i++) {
      elements[i] = new Element(0, new AtomicInteger(0));
    }
  }

  private int next(int index) {
    return index + 1 == elements.length ? 0 : index + 1;
  }

  private synchronized void update() {
    long currentTime = System.currentTimeMillis() / unitSpanMs;
    int passed = (int) (currentTime - elements[currentIndex].time);
    passed = passed > elements.length ? elements.length : passed;
    for (int i = 0; i < passed; i++) {
      currentIndex = next(currentIndex);
      elements[currentIndex].time = i == passed - 1 ? currentTime : 0;
      elements[currentIndex].count.set(0);
    }
  }

  /**
   * Increase count by 1.
   */
  public void mark() {
    if ((System.currentTimeMillis() / unitSpanMs - elements[currentIndex].time) > 0) {
      update();
    }
    elements[currentIndex].count.incrementAndGet();
  }

  /**
   * Get the marked count inside the time window.
   */
  public int count() {
    if ((System.currentTimeMillis() / unitSpanMs - elements[currentIndex].time) > 0) {
      update();
    }
    int sum = 0;
    for (Element element : elements) {
      sum += element.count.get();
    }
    return sum;
  }

  /**
   * Get the counts in each time unit span as a list.
   */
  public List<Integer> getAll() {
    List<Integer> values = new ArrayList<>(elements.length);
    int index = next(currentIndex);
    for (int i = 0; i < elements.length; i++) {
      values.add(elements[index].count.get());
      index = next(index);
    }
    return values;
  }

  private class Element {
    long time;
    AtomicInteger count;

    Element(long time, AtomicInteger count) {
      this.time = time;
      this.count = count;
    }
  }
}
