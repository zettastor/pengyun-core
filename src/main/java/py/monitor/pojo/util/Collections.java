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

package py.monitor.pojo.util;

import java.util.Arrays;
import java.util.EnumSet;

public class Collections {
  private Collections() {
  }

  static <E extends Enum<E>> EnumSet<E> setOf(Class<E> clazz, E... elements) {
    if (elements == null || elements.length == 0) {
      return EnumSet.noneOf(clazz);
    } else {
      return EnumSet.copyOf(Arrays.asList(elements));
    }

  }
}
