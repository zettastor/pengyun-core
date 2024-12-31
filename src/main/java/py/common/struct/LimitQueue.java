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

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;

public class LimitQueue<T> {
  private final int limit;

  private LinkedBlockingDeque<T> queue = new LinkedBlockingDeque<>();

  public LimitQueue(int limit) {
    this.limit = limit;
  }

  public boolean offer(T t) {
    boolean dropHead = false;
    if (queue.size() >= limit) {
      queue.poll();
      dropHead = true;
    }
    queue.offer(t);
    return dropHead;
  }

  public T getLast() {
    T element = queue.getLast();
    return element;
  }

  public T getFirst() {
    T element = queue.getFirst();
    return element;
  }

  public int size() {
    return queue.size();
  }

  public int getLimit() {
    return limit;
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("LimitQueue:\n");
    synchronized (queue) {
      Iterator<T> iterator = queue.iterator();
      while (iterator.hasNext()) {
        T element = iterator.next();
        stringBuilder.append("element:[");
        stringBuilder.append(element);
        stringBuilder.append("],\n");

      }
    }
    return stringBuilder.toString();
  }

  // add just for unit test
  public LinkedBlockingDeque<T> getQueue() {
    return queue;
  }

}
