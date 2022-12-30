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

package py.monitor.pojo.management.helper;

import org.junit.Assert;
import org.junit.Test;
import py.test.TestBase;

public class DescriptionEnDecoderTest extends TestBase {
  @Test
  public void testFormat() {
    DescriptionEnDecoder descriptionEnDecoder = new DescriptionEnDecoder();
    descriptionEnDecoder
        .formatBy("DescriptionEnDecoder [range=[0,1], unitOfMeasurement=byte, description=a]");

    Assert.assertEquals(descriptionEnDecoder.getRange(), "[0,1]");
    Assert.assertEquals(descriptionEnDecoder.getUnitOfMeasurement(), "byte");
    Assert.assertEquals(descriptionEnDecoder.getDescription(), "a");
  }

  @Test
  public void testToString() {
    DescriptionEnDecoder descriptionEnDecoder = new DescriptionEnDecoder();
    descriptionEnDecoder.setRange("[0,1]");
    descriptionEnDecoder.setUnitOfMeasurement("byte");
    descriptionEnDecoder.setDescription("a");

    logger.debug("{}", descriptionEnDecoder.toString());
    Assert.assertEquals(descriptionEnDecoder.toString(),
        "DescriptionEnDecoder [range=[0,1], unitOfMeasurement=byte, description=a]");
  }
}
