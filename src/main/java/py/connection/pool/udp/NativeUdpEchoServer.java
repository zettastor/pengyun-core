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

package py.connection.pool.udp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.struct.EndPoint;

public class NativeUdpEchoServer implements UdpServer {
  private static final Logger logger = LoggerFactory.getLogger(NioUdpEchoServer.class);
  private final EndPoint endPoint;
  private UDPEchoServer echoServerNative;
  private int fd = -1;

  public NativeUdpEchoServer(EndPoint endPoint) {
    this.endPoint = endPoint;
    this.echoServerNative = new UDPEchoServer();
  }

  public void startEchoServer() throws Exception {
    if (fd != -1) {
      logger.error("echo server has started, fd {}", fd);
      throw new Exception();
    }
    fd = echoServerNative.startEchoServer(endPoint.getPort());
    logger.warn("start server={}", fd);
  }

  public void stopEchoServer() {
    if (fd != -1) {
      echoServerNative.stopEchoServer(fd);
      logger.warn("stop server={}", fd);
      fd = -1;
    }
  }
}
