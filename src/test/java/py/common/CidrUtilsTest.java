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
