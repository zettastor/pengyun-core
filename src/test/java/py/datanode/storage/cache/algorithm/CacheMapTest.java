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
