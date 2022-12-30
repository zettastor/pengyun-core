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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import org.junit.Test;
import py.test.TestBase;

public class LocationTest extends TestBase {
  @Test
  public void basicTest() throws Exception {
    Location location = new Location("region1", "dc1", "rack1", "host1");

    String locationStr = location.toString();
    assertEquals("r=region1;d=dc1;c=rack1;h=host1", locationStr);

    Location newLocation = Location.fromString(locationStr);

    assertEquals("region1", newLocation.getRegion());
    assertEquals("dc1", newLocation.getDc());
    assertEquals("rack1", newLocation.getRack());
    assertEquals("host1", newLocation.getHost());
  }

  @Test
  public void emptyFieldTest() throws Exception {
    Location location = new Location("test", "dc1", null, null);

    String locationStr = location.toString();
    assertEquals("r=test;d=dc1", locationStr);

    Location newLocation = Location.fromString(locationStr);

    assertEquals("test", newLocation.getRegion());
    assertEquals("dc1", newLocation.getDc());
    assertNull(newLocation.getRack());
    assertNull(newLocation.getHost());

    location = new Location("test", "dc1", "rack1", null);

    locationStr = location.toString();
    assertEquals("r=test;d=dc1;c=rack1", locationStr);

    newLocation = Location.fromString(locationStr);

    assertEquals("test", newLocation.getRegion());
    assertEquals("dc1", newLocation.getDc());
    assertEquals("rack1", newLocation.getRack());
    assertNull(newLocation.getHost());
  }

  @Test
  public void testBadFieldName() {
    try {
      Location newLocation = Location.fromString("a=test;d=dc1;d=aa");
      fail();
    } catch (Exception e) {
      logger.error("caught exception", e);
    }
  }

  @Test
  public void testDupField() throws Exception {
    try {
      Location newLocation = Location.fromString("r=test;d=dc1;d=aa");
      fail();
    } catch (Exception e) {
      logger.error("caught exception", e);
    }

    try {
      Location newLocation = Location.fromString("r=test;c=dc1;c=bb;d=aa");
      fail();
    } catch (Exception e) {
      logger.error("caught exception", e);
    }
  }

  @Test
  public void testMapJson() throws Exception {
    Location location = new Location("region1", "dc1", "rack1", "host1");

    ObjectMapper mapper = new ObjectMapper();

    SimpleModule mod = new SimpleModule();

    mod.addDeserializer(Location.class, new Location.LocationDeserializer());
    mod.addSerializer(Location.class, new Location.LocationSerializer());
    mapper.registerModule(mod);
    String jsonLocationStr = null;

    try {
      jsonLocationStr = mapper.writeValueAsString(location);
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }

    Location parsedLocation = null;
    try {
      parsedLocation = mapper.readValue(jsonLocationStr, Location.class);

    } catch (IOException e) {
      fail(e.getMessage());
    }
    assertEquals(location, parsedLocation);
  }

  @Test
  public void testMapJsonWithEmptyDc() throws Exception {
    String jsonLocationStr = "{\"l\":\"r=1;d=;c=3;h=4\"}";

    ObjectMapper mapper = new ObjectMapper();

    SimpleModule mod = new SimpleModule();

    mod.addDeserializer(Location.class, new Location.LocationDeserializer());
    mod.addSerializer(Location.class, new Location.LocationSerializer());
    mapper.registerModule(mod);

    try {
      Location parsedLocation = mapper.readValue(jsonLocationStr, Location.class);
      fail();
    } catch (Exception e) {
      logger.error("caught exception", e);
    }
  }
}