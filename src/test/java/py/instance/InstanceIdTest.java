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

package py.instance;

import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import py.test.TestBase;

public class InstanceIdTest extends TestBase {
  @Test
  public void testMapJson() throws Exception {
    InstanceId ii = new InstanceId();
    ObjectMapper mapper = new ObjectMapper();
    String str = mapper.writeValueAsString(ii);
    logger.warn(str);

    InstanceId newInstanceId = mapper.readValue(str, InstanceId.class);
    assertTrue(ii.equals(newInstanceId));
  }
}
