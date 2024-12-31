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

package py.license;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.test.TestBase;

public class UtilsTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(UtilsTest.class);

  @Test
  public void compositeAndSplitTest() {
    String str = getRandomString(1100);
    logger.debug("str is : {}", str);
    String[] strSection = Utils.splitStr(str);
    logger.debug("str section is : {}", Arrays.toString(strSection));
    String strAfter = Utils.compsiteStr(strSection);
    logger.debug("str after is : {}", strAfter);
    assertEquals(str, strAfter);
  }
}
