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

