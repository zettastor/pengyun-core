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

package py.algorithm.policy;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.netty.client.AsyncResponseHandler;

public class CacheMap implements ObjectKeyToArrayIndexMap<java.lang.Long> {
  private static final Logger logger = LoggerFactory.getLogger(CacheMap.class);

  private static final int MAX_HASH_CAPACITY = 1 << 30;
  private static final float LOAD_FACTOR = 0.5f;

  protected final List<Long2IntOpenHashMap> originList;
  private final int mapCount;

  public CacheMap(int capacity) {
    this(capacity, MAX_HASH_CAPACITY);
  }

  public CacheMap(int capacity, int maxCapacity) {
    Validate.isTrue(maxCapacity <= MAX_HASH_CAPACITY);
    long capacitySize = HashCommon.bigArraySize(capacity, LOAD_FACTOR);
    mapCount = (int) Math.ceil((double) capacitySize / maxCapacity);
    originList = new ArrayList<>(mapCount);
    for (int i = 0; i < mapCount; i++) {
      Long2IntOpenHashMap origin = new Long2IntOpenHashMap(capacity / mapCount, LOAD_FACTOR);
      originList.add(origin);
    }
   
   
   
  }

  private Long2IntOpenHashMap getOrigin(long key) {
    return originList.get((int) (key % mapCount));
  }

  @Override
  public int get(Long key) {
    try {
      return getOrigin(key).get(key.longValue());
    } finally {
      logger.info("nothing need to do here");
    }
  }

  public int getEmptyValue() {
    return originList.get(0).defaultReturnValue();
  }

  @Override
  public void put(Long key, int value) {
    try {
      getOrigin(key).put(key.longValue(), value);
    } finally {
      logger.info("nothing need to do here");
    }
  }

  @Override
  public void remove(Long key) {
    try {
      getOrigin(key).remove(key.longValue());
    } finally {
      logger.info("nothing need to do here");
    }
  }

  @Override
  public Iterator<Long> keySetIterator() {
    return new CacheIterator();
  }

  private class CacheIterator implements Iterator<Long> {
    private int startIndex = 0;
    private LongIterator startIterator = originList.get(0).keySet().iterator();

    @Override
    public boolean hasNext() {
      if (startIterator.hasNext()) {
        return true;
      }

      int maxIndex = originList.size() - 1;
      while (++startIndex <= maxIndex) {
        startIterator = originList.get(startIndex).keySet().iterator();
        if (startIterator.hasNext()) {
          return true;
        }
      }
      return false;
    }

    @Override
    public Long next() {
      return startIterator.nextLong();
    }
  }
}
