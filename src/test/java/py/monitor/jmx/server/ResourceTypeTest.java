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

package py.monitor.jmx.server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.test.TestBase;

public class ResourceTypeTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(ResourceTypeTest.class);

  @Test
  public void testGetRegex() {
    logger.debug("{}", ResourceType.getRegex());
    Assert.assertEquals("(NONE|VOLUME|STORAGE_POOL|STORAGE|GROUP|MACHINE|JVM|NETWORK|DISK)",
        ResourceType.getRegex());
  }

  @Test
  public void getLocalIpAddress() throws Exception {
    Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
    for (; n.hasMoreElements(); ) {
      NetworkInterface e = n.nextElement();

      Enumeration<InetAddress> a = e.getInetAddresses();
      for (; a.hasMoreElements(); ) {
        InetAddress addr = a.nextElement();
        System.out.println("  " + addr.getHostAddress());
      }
    }
  }
}
