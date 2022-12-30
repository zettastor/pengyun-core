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

package py.common.counter;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.instance.InstanceId;
import py.test.TestBase;

public class TreeSetObjectCounterTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(TreeSetObjectCounter.class);

  @Test
  public void testUnique() {
    TreeSetObjectCounter<InstanceId> counter = new TreeSetObjectCounter<>();
    InstanceId id1 = new InstanceId(1);
    InstanceId id2 = new InstanceId(2);
    InstanceId id3 = new InstanceId(3);
    InstanceId id4 = new InstanceId(4);

    counter.increment(id1);
    counter.increment(id1);
    counter.increment(id1);
    counter.increment(id1);

    counter.increment(id2);
    counter.increment(id2);
    counter.increment(id2);

    counter.increment(id3);
    counter.increment(id3);
    counter.increment(id3);
    counter.increment(id3);
    counter.increment(id3);
    logger.warn("{} ", counter);
  }
}