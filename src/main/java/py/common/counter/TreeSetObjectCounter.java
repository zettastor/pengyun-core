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
