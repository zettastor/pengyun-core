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
