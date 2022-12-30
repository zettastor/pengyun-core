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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrequentlyUsedStrings {
  protected static final Logger log = LoggerFactory.getLogger(FrequentlyUsedStrings.class);

  protected static final int MAX_ENTRIES = 200;

  private static int maxEntries = MAX_ENTRIES;

  private static ConcurrentMap<String, String> stringPoool = new ConcurrentHashMap<>();

  public static String get(String s) {
    if (s == null) {
      return null;
    }
    String known = stringPoool.get(s);
    if (known != null) {
      return known;
    }
    // Although we don't synchronize maxEntries, that is ok. We don't need to be perfectly accurate.
    if (stringPoool.size() < maxEntries) {
      known = stringPoool.putIfAbsent(s, s);
      if (known != null) {
        return known;
      }

    }
    return s;
  }

  public static int getMaxEntries() {
    return maxEntries;
  }

  public static void setMaxEntries(int max) {
    maxEntries = max;
  }
}
