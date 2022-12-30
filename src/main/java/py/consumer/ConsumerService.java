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

package py.consumer;

import java.util.Collection;

public interface ConsumerService<E> {
  void start();

  void stop();

  boolean submit(E element);

  default int submit(Collection<E> elements) {
    int successCount = 0;
    for (E element : elements) {
      successCount += submit(element) ? 1 : 0;
    }
    return successCount;
  }

}
