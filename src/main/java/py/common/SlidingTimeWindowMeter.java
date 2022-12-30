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
