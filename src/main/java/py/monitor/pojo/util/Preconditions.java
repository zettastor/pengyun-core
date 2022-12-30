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

public class Preconditions {
  private Preconditions() {
  }

  public static <E> E notNull(E obj) {
    return notNull(obj, null);
  }

  public static <E> E notNull(E obj, String msg) {
    if (obj == null) {
      throw (msg == null) ? new NullPointerException() : new NullPointerException(msg);
    }
    return obj;
  }

  public static void assertTrue(boolean b) {
    assertTrue(b, null);
  }

  public static void assertTrue(boolean b, String msg) {
    if (!b) {
      throw (msg == null) ? new IllegalArgumentException() : new IllegalArgumentException(msg);
    }
  }

  public static String notEmpty(String s) {
    return notEmpty(s, null);
  }

  public static String notEmpty(String s, String msg) {
    notNull(s, msg);
    if (s.isEmpty()) {
      throw (msg == null) ? new NullPointerException("empty") : new NullPointerException(msg);
    }
    return s;
  }
}
