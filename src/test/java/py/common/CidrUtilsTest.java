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

package py.common;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.test.TestBase;

public class CidrUtilsTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(CidrUtilsTest.class);

  @Override
  public void init() throws Exception {
    super.init();
  }

  @Test
  public void testIpv6PrefixMatches() throws Exception {
    CidrUtils utils;

    utils = new CidrUtils("fe80:0:0:0:0:0:c0a8:1/120");
    Assert.assertTrue(utils.isInRange("fe80:0:0:0:0:0:c0a8:11"));

    utils = new CidrUtils("fe80:0:0:0:0:0:c0a8:1/120");
    Assert.assertFalse(utils.isInRange("fe81:0:0:0:0:0:c0a8:11"));

    utils = new CidrUtils("fe80:0:0:0:0:0:c0a8:1/128");
    Assert.assertFalse(utils.isInRange("fe80:0:0:0:0:0:c0a8:11"));

    utils = new CidrUtils("192.168.122.1/32");
    Assert.assertFalse(utils.isInRange("fe80:0:0:0:0:0:c0a8:11"));

    utils = new CidrUtils("fe80::ec4:7aff:fe4b:ee5e/64");
    Assert.assertTrue(utils.isInRange("fe80::d1f7:f18:116a:13f4"));
  }
}
