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

import com.beust.jcommander.Parameter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.function.Consumer;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.LoggerConfiguration;
import py.common.struct.EndPoint;

public class NioUdpEchoServer implements UdpServer {
  private static final Logger logger = LoggerFactory.getLogger(NioUdpEchoServer.class);
  private final EndPoint endPoint;
  private Thread echoThread;
  private boolean stop = false;
  private DatagramSocket server;
  private boolean sendBack = true;
  private Consumer<byte[]> receiveListener = bytes -> {
  };

  public NioUdpEchoServer(EndPoint endPoint) {
    this.endPoint = endPoint;
  }

  public static void main(String[] args) {
    LoggerConfiguration.getInstance().appendFile("/root/udpServer.log", Level.WARN);

    EndPoint localEnd = new EndPoint();
    localEnd.setPort(54321);
    NioUdpEchoServer udpServer = new NioUdpEchoServer(localEnd);

    try {
      udpServer.startEchoServer();
    } catch (Exception e) {
      logger.error("UDP Server Startup Exception" + e);
    }

    logger.warn("UDP Server startup successfully!!");

  }

  public void setSendBack(boolean sendBack) {
    this.sendBack = sendBack;
  }

  public void setReceiveListener(Consumer<byte[]> receiveListener) {
    this.receiveListener = receiveListener;
  }

  private void receiveAndEcho(byte[] buffer) throws IOException {
    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
    server.receive(receivePacket);

    String receiveMsg = new String(receivePacket.getData(), 0, receivePacket.getLength());
    logger.debug("====receive from client, buffer length={}, message length={}, buffer={}",
        receivePacket.getLength(), receiveMsg.length(), receiveMsg.trim());

    long connectBegin = System.currentTimeMillis();
    SimpleDateFormat timeStampStrFormat = new SimpleDateFormat("HH:mm:ss,SSS");
    String timeStampStr = "reply time=" + timeStampStrFormat.format(new java.util.Date());
    byte[] newPktBuff = timeStampStr.getBytes();
    System.arraycopy(newPktBuff, 0, buffer, receivePacket.getLength(), newPktBuff.length);

    if (sendBack) {
      int length = receiveMsg.length() + newPktBuff.length;

      DatagramPacket sendPacket = new DatagramPacket(buffer, length,
          receivePacket.getSocketAddress());
      try {
        server.send(sendPacket);
        logger.debug("====send to client, length={}, buffer={}", length, sendPacket.getData());
      } catch (Exception e) {
        logger.error("UDP detect packet send failed.", e);
      }
    }
    receiveListener.accept(buffer);
  }

  public void startEchoServer() throws Exception {
    logger.warn("starting to listen on {}", endPoint.getPort());
    this.server = new DatagramSocket(new InetSocketAddress(endPoint.getPort()));
    this.echoThread = new Thread("server-echo") {
      public void run() {
        while (!stop) {
          byte[] buffer = new byte[512];
          try {
            receiveAndEcho(buffer);
          } catch (IOException e) {
            logger.warn("caught an exception", e);
          }
        }
      }
    };
    this.echoThread.start();
  }

  public void stopEchoServer() {
    this.stop = true;
    if (server != null) {
      server.close();
    }

    try {
      this.echoThread.join();
    } catch (InterruptedException e) {
      logger.warn("caught an exception", e);
    }
  }

  private static class CommandLineArgs {
    public static final String LISTENINGPORT = "--listeningPort";
    @Parameter(names = LISTENINGPORT, description = "Listening Port of UDP Server", required = true)
    public String listeningPort;

    @Parameter(names = "--help", help = true)
    private boolean help;

    @Override
    public String toString() {
      return "CommandLineArgs{" + "listeningPort='" + listeningPort + '\'' + ", help=" + help + '}';
    }
  }
}