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

package py.netty.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.netty.exception.DisconnectionException;

public class NetworkDetect {
  private static final Logger logger = LoggerFactory.getLogger(NetworkDetect.class);

  public static void ping(String ip, int timeout) throws DisconnectionException {
    try {
      if (!InetAddress.getByName(ip).isReachable(timeout)) {
        logger.warn("ip: {} is not reachable, timeout: {}", ip, timeout);
        throw new DisconnectionException("ip: " + ip + ", timeout: " + timeout);
      }
    } catch (UnknownHostException e) {
      logger.warn("there is no host: {}, {}", ip, timeout, e);
      throw new DisconnectionException("ip: " + ip + ", timeout: " + timeout);
    } catch (IOException e) {
      logger.warn("io exception: {}, {}", ip, timeout, e);
      throw new DisconnectionException("ip: " + ip + ", timeout: " + timeout);
    }
  }

  public static void ping(String ip, int port, int timeout) throws DisconnectionException {
    ping(ip, timeout);
    connect(ip, port, timeout);
  }

  public static void connect(String ip, int port, int timeout) throws DisconnectionException {
    Socket socket = new Socket();
    try {
      socket.connect(new InetSocketAddress(ip, port), timeout);
    } catch (Exception e) {
      logger.warn("caught an exception when connecting", e);
      throw new DisconnectionException("ip: " + ip + ", port: " + port + ", timeout: " + timeout);
    } finally {
      try {
        socket.close();
      } catch (IOException e) {
        logger.warn("caught an exception when closing the socket", e);
      }
    }
  }

  public static boolean myselfAlive(String hostName) {
    InetAddress inetAddress = null;
    try {
      NetworkInterface networkInterface = NetworkInterface
          .getByInetAddress(InetAddress.getByName(hostName));
      inetAddress = InetAddress.getLocalHost();
      if (networkInterface == null || !networkInterface.isUp()) {
        logger.error("the hostname is {}, the address is {}", hostName, inetAddress);
        return false;
      }
    } catch (SocketException | UnknownHostException e) {
      logger.error("the hostname is {}, the address is {}", hostName, inetAddress);
      return false;
    }
    return true;
  }

}
