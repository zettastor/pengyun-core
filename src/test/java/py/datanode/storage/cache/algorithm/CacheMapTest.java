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

package py.datanode.storage.cache.algorithm;

import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.lang.Validate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import py.algorithm.policy.CacheMap;

@RunWith(Parameterized.class)
public class CacheMapTest {
  private long key;
  private int value;

  public CacheMapTest(int inputKey, int inputValue) {
    this.key = inputKey;
    this.value = inputValue;
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {0, 0}, {1, 1}, {2, 1}, {3, 2}, {4, 3}, {5, 5}, {6, 8}
    });
  }

  @Test
  public void testCacheMap1() {
    CacheMap cacheMap = new CacheMap(50, 128);
    cacheMap.put(key, value);
    Validate.isTrue(value == cacheMap.get(key));
    cacheMap.remove(key);
    Validate.isTrue(cacheMap.get(key) == cacheMap.getEmptyValue());
  }

  @Test
  public void testCacheMap2() {
    CacheMap cacheMap = new CacheMap(50, 129);
    cacheMap.put(key, value);
    Validate.isTrue(value == cacheMap.get(key));

    cacheMap.remove(key);
    Validate.isTrue(cacheMap.get(key) == cacheMap.getEmptyValue());
  }

  @Test
  public void testCacheMap3() {
    CacheMap cacheMap = new CacheMap(50, 126);
    cacheMap.put(key, value);
    Validate.isTrue(value == cacheMap.get(key));

    cacheMap.remove(key);
    Validate.isTrue(cacheMap.get(key) == cacheMap.getEmptyValue());
  }
}
