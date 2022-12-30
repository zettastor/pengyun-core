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

package py.iet.file.mapper.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.InetAddress;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.iet.file.mapper.SessionFileMapper;
import py.iet.file.mapper.SessionFileMapper.Initiator;
import py.iet.file.mapper.SessionFileMapper.Session;
import py.test.TestBase;

public class SessionFileMapperTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(SessionFileMapperTest.class);

  private InetAddress inetAddr117;
  private InetAddress inetAddrIpv6;
  private InetAddress inetAddr16;

  private SessionFileMapper sessionFileMapper = new SessionFileMapper();

  @Before
  public void init() throws Exception {
    super.init();

    inetAddr117 = InetAddress.getByName("10.0.1.117");
    inetAddrIpv6 = InetAddress.getByName("[fe80:0000:0000:0000:5054:00ff:fe54:4d0c]");
    inetAddr16 = InetAddress.getByName("10.0.1.16");

    String filePath = "/tmp/SessionFileMapperTest";

    sessionFileMapper.setFilePath(filePath);

    BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(filePath));
    bufferWriter.write("tid:1 name:iqn.test");
    bufferWriter.newLine();
    bufferWriter
        .write("        sid:562949990973952 initiator:iqn.1993-08.org.debian:01:cbafde8356bd");
    bufferWriter.newLine();
    bufferWriter.write("                cid:0 ip:10.0.1.117 state:active hd:none dd:none");
    bufferWriter.newLine();
    bufferWriter
        .write("        sid:282574492336640 initiator:iqn.1993-08.org.debian:01:cbafde8356bd");
    bufferWriter.newLine();
    bufferWriter.write(
        "                cid:0 ip:"
            + "[fe80:0000:0000:0000:5054:00ff:fe54:4d0c] state:active hd:none dd:none");
    bufferWriter.newLine();
    bufferWriter
        .write("        sid:281475014263296 initiator:iqn.1993-08.org.debian:01:7767874e9dae");
    bufferWriter.newLine();
    bufferWriter.write("                cid:0 ip:10.0.1.16 state:active hd:none dd:none");
    bufferWriter.flush();
    bufferWriter.close();
  }

  @Test
  public void testSessionFileMapper() {
    sessionFileMapper.load();

    Assert.assertTrue(sessionFileMapper.getSessionList().size() == 1);
    Session session = sessionFileMapper.getSessionList().get(0);
    Assert.assertTrue(session.getTargetName().equals("iqn.test"));
    Assert.assertTrue(session.getInitiatorList().size() == 3);
    for (Initiator initiator : session.getInitiatorList()) {
      logger.debug("ip of initiator: {}", initiator.getIp());
      Assert.assertTrue(initiator.getCid() == 0);
      Assert.assertTrue(initiator.getIp().equals(inetAddr117.getHostAddress())
          || initiator.getIp().equals(inetAddr16.getHostAddress())
          || initiator.getIp().equals(inetAddrIpv6.getHostAddress()));
      Assert.assertTrue(
          initiator.getSid() == 562949990973952L || initiator.getSid() == 281475014263296L
              || initiator.getSid() == 282574492336640L);
    }
  }
}
