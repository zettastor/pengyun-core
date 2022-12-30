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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public interface ObjectCounter<T> extends Comparator<T> {
  long get(T t);

  void increment(T t);

  void increment(T t, long n);

  void decrement(T t);

  void decrement(T t, long n);

  void set(T t, long n);

  boolean remove(T t);

  T max();

  long maxValue();

  T min();

  long minValue();

  long total();

  int size();

  void clear();

  Iterator<T> iterator();

  Iterator<T> iterator(Comparator<T> comparator);

  Iterator<T> descendingIterator();

  Collection<T> getAll();

  ObjectCounter<T> deepCopy();

}
