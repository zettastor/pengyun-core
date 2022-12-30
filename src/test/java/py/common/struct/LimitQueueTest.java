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
