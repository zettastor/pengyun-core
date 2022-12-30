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
