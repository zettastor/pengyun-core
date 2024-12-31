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
