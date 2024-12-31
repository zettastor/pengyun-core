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
