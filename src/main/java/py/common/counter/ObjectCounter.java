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
