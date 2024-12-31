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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.test.TestBase;

public class LimitQueueTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(LimitQueueTest.class);

  @Test
  public void testLimitQueueAndPrint() {
    int limitQueueCount = 5;
    int moreCount = 3;
    LimitQueue<Integer> testLimitQueue = new LimitQueue<>(limitQueueCount);

    List<Integer> allTestString = new ArrayList<>();
    for (int i = 0; i < limitQueueCount + moreCount; i++) {
      if (i < limitQueueCount) {
        assertTrue(testLimitQueue.size() == i);
      } else {
        assertTrue(testLimitQueue.size() == limitQueueCount);
      }

      boolean drop = testLimitQueue.offer(i);
      allTestString.add(i);
      if (i < limitQueueCount) {
        assertFalse(drop);
      } else {
        assertTrue(drop);
      }
    }
    logger.warn("print:{}", testLimitQueue);

  }
}
