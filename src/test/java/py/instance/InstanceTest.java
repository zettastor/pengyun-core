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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import py.common.struct.EndPoint;
import py.common.struct.EndPointParser;
import py.test.TestBase;

public class InstanceTest extends TestBase {
  @Test
  public void basicTest() throws Exception {
    Location location = new Location("c1", "dc1", "rack1", "host1");
    Instance instance = new Instance(new InstanceId(1L), new Group(1), location, "testing",
        InstanceStatus.HEALTHY);
    instance.putEndPointByServiceName(PortType.CONTROL, new EndPoint("localhost", 100));

    ObjectMapper mapper = new ObjectMapper();
    byte[] buf = mapper.writeValueAsBytes(instance);
    logger.info(mapper.writeValueAsString(instance));

    Instance newInstance = mapper.readValue(buf, Instance.class);
    assertTrue(newInstance.equals(instance));
  }

  @Test
  public void testMapJson() throws Exception {
    Map<Integer, EndPoint> endPoints = new HashMap<Integer, EndPoint>();
    endPoints.put(0, new EndPoint("localhost", 100));
    endPoints.put(1, new EndPoint("10.0.1.17", 120));
    endPoints.put(2, new EndPoint("10.0.1.19", 120));

    ObjectMapper mapper = new ObjectMapper();
    String str = mapper.writeValueAsString(endPoints);
    logger.info(str);

    TypeReference<HashMap<Integer, EndPoint>> typeRef = new
        TypeReference<HashMap<Integer, EndPoint>>() {
        };
    Map<Integer, EndPoint> temp = mapper.readValue(str, typeRef);
    assertTrue(temp.size() == endPoints.size());
    for (Entry<Integer, EndPoint> entry : temp.entrySet()) {
      assertTrue(entry.getValue().equals(endPoints.get(entry.getKey())));
    }
  }

  @Test
  public void instanceWithTwoEndPoint() throws Exception {
    Location location = new Location("c1", "dc1", "rack1", "host1");
    Instance instance = new Instance(new InstanceId(1L), new Group(1), location, "testing",
        InstanceStatus.HEALTHY);
    instance.putEndPointByServiceName(PortType.CONTROL, new EndPoint("localhost", 100));
    instance.putEndPointByServiceName(PortType.IO, new EndPoint("10.0.1.17", 120));
    instance.putEndPointByServiceName(PortType.MONITOR, null);
    instance.putEndPointByServiceName(PortType.IO, new EndPoint("10.0.1.18", 120));

    ObjectMapper mapper = new ObjectMapper();
    byte[] buf = mapper.writeValueAsBytes(instance);
    logger.info(mapper.writeValueAsString(instance));

    Instance newInstance = mapper.readValue(buf, Instance.class);
    assertTrue(newInstance.equals(instance));
  }

  @Test
  public void testEndPoint() throws Exception {
    String strEndPoint = "2222";
    EndPoint endPoint1 = EndPointParser.parseLocalEndPoint(strEndPoint,
        InetAddress.getLocalHost().getHostAddress());
    strEndPoint = ":2222";
    EndPoint endPoint2 = EndPointParser.parseLocalEndPoint(strEndPoint,
        InetAddress.getLocalHost().getHostAddress());
    assertEquals(endPoint1, endPoint2);

    EndPoint endPoint3 = new EndPoint("10.0.1.18", 2222);
    EndPoint endPoint4 = EndPointParser.parseLocalEndPoint(endPoint3.toString(),
        InetAddress.getLocalHost().getHostAddress());
    assertEquals(endPoint3, endPoint4);

    try {
      strEndPoint = "";
      EndPointParser.parseLocalEndPoint(strEndPoint, InetAddress.getLocalHost().getHostAddress());
      fail();
    } catch (Exception e) {
      logger.error("caught exception", e);
    }
  }

  @Test
  public void compareFailedInstanceWithSmallHb() throws Exception {
    Location location = new Location("c1", "dc1", "rack1", "host1");
    Instance instance1 = new Instance(new InstanceId(1L), new Group(1), location, "testing",
        InstanceStatus.FAILED);
    instance1.putEndPointByServiceName(PortType.CONTROL, new EndPoint("localhost", 100));
    instance1.setHeartBeatCounter(1);

    Instance instance2 = new Instance(new InstanceId(1L), new Group(1), location, "testing",
        InstanceStatus.HEALTHY);
    instance2.putEndPointByServiceName(PortType.CONTROL, new EndPoint("localhost", 100));
    instance2.setHeartBeatCounter(99);

    Assert.assertTrue(instance1.compareTo(instance2) < 0);
  }

  @Test
  public void testSetVariousTimeToInstance() throws Exception {
    Location location = new Location("c1", "dc1", "rack1", "host1");
    Instance instance = new Instance(new InstanceId(1L), new Group(1), location, "test",
        InstanceStatus.HEALTHY);

    Assert.assertFalse(instance.isSetLastGossipTime());
    Assert.assertFalse(instance.isSetExpirationTime());
    Assert.assertFalse(instance.isSetFailedTime());

    instance.setLastGossipTime(0L);
    instance.setExpirationTime(0L);
    instance.setFailedTime(0L);

    Assert.assertTrue(instance.isSetLastGossipTime());
    Assert.assertTrue(instance.isSetExpirationTime());
    Assert.assertTrue(instance.isSetFailedTime());
  }
}
