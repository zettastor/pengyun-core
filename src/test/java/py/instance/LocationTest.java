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