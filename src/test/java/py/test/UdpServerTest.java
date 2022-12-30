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

package py.test;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang.Validate;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import py.common.struct.EndPoint;
import py.connection.pool.udp.UDPEchoServer;

public class UdpServerTest extends TestBase {
  protected static org.slf4j.Logger logger = LoggerFactory.getLogger(TestBase.class);

  @Before
  public void addSystemProperty() {
    addSystemProperty("java.library.path",
        System.getProperty("user.dir") + "/src/main/resources");
  }

  private void addSystemProperty(String key, String value) {
    try {
      Field field = ClassLoader.class.getDeclaredField("usr_paths");
      field.setAccessible(true);
      String[] properties = (String[]) field.get(null);
      for (String str : properties) {
        if (value.equals(str)) {
          return;
        }
      }
      String[] propertiesDest = new String[properties.length + 1];
      System.arraycopy(properties, 0, propertiesDest, 0, properties.length);
      propertiesDest[properties.length] = value;
      field.set(null, propertiesDest);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void test() throws Exception {
    String path = System.getProperty("java.library.path");
    logger.warn("path={}", path);
    UDPEchoServer udpServer = new UDPEchoServer();
    if (udpServer == null) {
      return;
    }
    int id = udpServer.startEchoServer(51234);
    if (id == 0) {
      return;
    }

    DatagramSocket clientSocket = new DatagramSocket(1818);
    if (clientSocket == null) {
      fail();
    }

    EndPoint serviceEndPoint = new EndPoint("127.0.0.1", 51234);
    SocketAddress address = new InetSocketAddress(serviceEndPoint.getHostName(),
        serviceEndPoint.getPort());
    clientSocket.connect(address);

    AtomicBoolean interrupt = new AtomicBoolean(false);
    AtomicBoolean getRecieveResponse = new AtomicBoolean(false);
    new Thread(
        () -> {
          byte[] recieveData = new byte[512];
          DatagramPacket recievePacket = new DatagramPacket(recieveData, recieveData.length);
          while (true && !interrupt.get()) {
            try {
              clientSocket.receive(recievePacket);
              getRecieveResponse.set(true);
            } catch (IOException e) {
              logger.warn("io error");
              break;
            }
          }
        }
    ).start();

    byte[] sendData = new byte[512];
    DatagramPacket sendDataPacket = new DatagramPacket(sendData, sendData.length);
    clientSocket.send(sendDataPacket);

    Thread.sleep(3000);
    Validate.isTrue(getRecieveResponse.get());

    udpServer.pauseEchoServer();

    getRecieveResponse.set(false);
    clientSocket.send(sendDataPacket);
    Thread.sleep(3000);
    Validate.isTrue(!getRecieveResponse.get());

    udpServer.reviveEchoServer();
    clientSocket.send(sendDataPacket);
    Thread.sleep(3000);
    Validate.isTrue(getRecieveResponse.get());

    udpServer.stopEchoServer(id);
    interrupt.set(true);

  }
}
