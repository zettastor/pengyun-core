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