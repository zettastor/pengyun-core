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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.exception.InvalidFormatException;
import py.test.TestBase;

public class AbstractIdTest extends TestBase {

  private static final Logger logger = LoggerFactory.getLogger(AbstractIdTest.class);
  private final String prefix = "mi";

  public void init() throws Exception {
    super.init();
  }

  private final class MyId extends AbstractId {

    @Override
    public String printablePrefix() {
      return prefix;
    }

    public MyId(String id, boolean raw) {
      super(id, raw);
    }
  }

  @Test
  public void basicTest() throws Exception {
    String aid = "12345";
    MyId myId = new MyId(aid, true);
    Assert.assertEquals(12345L, myId.getId());

    String aid1 = prefix + "12345";
    String aid2 = prefix + "[12345";
    String aid3 = prefix + "12345]";
    try {
      myId = new MyId(aid1, false);
      myId = new MyId(aid2, false);
      myId = new MyId(aid3, false);
    } catch (Throwable e) {
      Assert.assertTrue(e instanceof InvalidFormatException);
    }

    String aid4 = prefix + "[12345]";
    MyId myId4 = new MyId(aid4, false);
    Assert.assertEquals(12345L, myId4.getId());
  }
}

