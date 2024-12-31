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

package py.common.counter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class TreeSetObjectCounter<T> implements ObjectCounter<T> {
  // use an long array to avoid creating too many instance of long
  protected final Map<T, long[]> counter = new HashMap<>();
  protected final TreeSet<T> set = new TreeSet<>(this.thenComparingInt(Object::hashCode));

  @Override
  public int compare(T o1, T o2) {
    return Long.compare(get(o1), get(o2));
  }

  @Override
  public long get(T t) {
    long[] var = counter.get(t);
    if (var == null) {
      return 0;
    } else {
      return var[0];
    }
  }

  @Override
  public void increment(T t) {
    long[] var = counter.get(t);
    if (var == null) {
      counter.put(t, new long[]{1L});
    } else {
      set.remove(t);
      var[0]++;
    }
    set.add(t);
  }

  @Override
  public void increment(T t, long n) {
    long[] var = counter.get(t);
    if (var == null) {
      counter.put(t, new long[]{n});
    } else {
      set.remove(t);
      var[0] += n;
    }
    set.add(t);
  }

  @Override
  public void decrement(T t) {
    long[] var = counter.get(t);
    if (var == null) {
      counter.put(t, new long[]{-1L});
    } else {
      set.remove(t);
      var[0]--;
    }
    set.add(t);
  }

  @Override
  public void decrement(T t, long n) {
    long[] var = counter.get(t);
    if (var == null) {
      counter.put(t, new long[]{-n});
    } else {
      set.remove(t);
      var[0] -= n;
    }
    set.add(t);
  }

  @Override
  public void set(T t, long n) {
    long[] var = counter.get(t);
    if (var == null) {
      counter.put(t, new long[]{n});
    } else {
      set.remove(t);
      var[0] = n;
    }
    set.add(t);
  }

  @Override
  public boolean remove(T t) {
    boolean removed = set.remove(t);
    counter.remove(t);
    return removed;
  }

  @Override
  public T max() {
    return set.last();
  }

  @Override
  public long maxValue() {
    return get(max());
  }

  @Override
  public T min() {
    return set.first();
  }

  @Override
  public long minValue() {
    return get(min());
  }

  @Override
  public long total() {
    long sum = 0;
    for (long[] var : counter.values()) {
      sum += var[0];
    }
    return sum;
  }

  @Override
  public int size() {
    return counter.size();
  }

  @Override
  public void clear() {
    set.clear();
    counter.clear();
  }

  @Override
  public Iterator<T> iterator() {
    return set.iterator();
  }

  public Iterator<T> iterator(Comparator<T> comparator) {
    TreeSet<T> cloneSet = new TreeSet<>(comparator.thenComparingInt(Object::hashCode));
    cloneSet.addAll(set);
    return cloneSet.iterator();
  }

  @Override
  public Iterator<T> descendingIterator() {
    return set.descendingIterator();
  }

  @Override
  public Collection<T> getAll() {
    List<T> all = new ArrayList<>();
    all.addAll(set);
    return all;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("TreeSetObjectCounter(")
        .append(System.identityHashCode(this)).append(")")
        .append(":{counters=[");
    for (T t : set) {
      if (get(t) != 0) {
        sb.append(t).append(t.hashCode()).append("=").append(get(t)).append(", ");
      }
    }
    sb.append("]}");
    return sb.toString();
  }

  @Override
  public TreeSetObjectCounter<T> deepCopy() {
    TreeSetObjectCounter<T> another = new TreeSetObjectCounter<>();
    for (Map.Entry<T, long[]> entryT : counter.entrySet()) {
      another.set(entryT.getKey(), entryT.getValue()[0]);
    }
    return another;
  }
}
