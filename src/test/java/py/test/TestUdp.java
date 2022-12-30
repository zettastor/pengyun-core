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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.ExchangeMessageCallback;
import py.common.UdpTransferImpl;
import py.common.struct.EchoFutureMessage;
import py.common.struct.EndPoint;
import py.common.struct.ExchangeMessage;

public class TestUdp extends TestBase {
  static final int SERVERPORT = 10112;
  static final int CLIENTPORT = 10113;
  private static final Logger logger = LoggerFactory.getLogger(TestUdp.class);
  static CountDownLatch latch;
  UdpTransferImpl udpServer;

  public void testUdpClientServerTimeout() throws InterruptedException {
    UdpTransferImpl udpClient = UdpTransferImpl.generateClient(3000);

    ByteBuffer bytebuf = ByteBuffer.allocate(30);
    for (int i = 0; i < 30; i++) {
      bytebuf.put((byte) ('a' + i));
    }

    EchoFutureMessage response = udpClient
        .exchangeMessage(bytebuf, new EndPoint("127.0.0.1", SERVERPORT));

    assertEquals(false, response.isArrived());

    ByteBuffer rcvbytebuf = response.readAvailableByteBuffer(3000);
    System.out.println("recv len" + rcvbytebuf.remaining());

    ServerMessageCallbackTimeout timeoutCallback =
        new ServerMessageCallbackTimeout(1500, true);
    UdpTransferImpl udpServer = UdpTransferImpl.generateServer(SERVERPORT, timeoutCallback);
    udpServer.releaseResource();
    udpClient.releaseResource();

  }

  public void testUdpClientServerDelay() throws InterruptedException {
    ByteBuffer bytebuf = ByteBuffer.allocate(30);
    for (int i = 0; i < 30; i++) {
      bytebuf.put((byte) ('a' + i));
    }

    UdpTransferImpl udpClient = UdpTransferImpl.generateClient(3000);
    EchoFutureMessage response = udpClient
        .exchangeMessage(bytebuf, new EndPoint("127.0.0.1", SERVERPORT));

    assertEquals(false, response.isArrived());

    ByteBuffer rcvbytebuf = response.readAvailableByteBuffer(1200);
    assertNotEquals(null, rcvbytebuf);

    assertEquals(30 + 1, rcvbytebuf.remaining());
    int len = rcvbytebuf.remaining();
    System.out.println("\n----------------");
    for (int i = 1; i < len - 1; i++) {
      assertEquals(bytebuf.get(i - 1), rcvbytebuf.get(i));
      System.out.printf("%2x,", rcvbytebuf.get(i));
    }
    System.out.println("\n----------------");

    ServerMessageCallbackTimeout timeoutCallback
        = new ServerMessageCallbackTimeout(1000, true);
    UdpTransferImpl udpServer = UdpTransferImpl.generateServer(SERVERPORT, timeoutCallback);
    udpServer.releaseResource();
    udpClient.releaseResource();
  }

  public void testUdpClientServerPacket() throws InterruptedException {
    ByteBuffer bytebuf = ByteBuffer.allocate(30);
    for (int i = 0; i < 30; i++) {
      bytebuf.put((byte) ('a' + i));
    }

    UdpTransferImpl udpClient = UdpTransferImpl.generateClient(3000);
    EchoFutureMessage response = udpClient
        .exchangeMessage(bytebuf, new EndPoint("127.0.0.1", SERVERPORT + 1));
    ByteBuffer rcvbytebuf = response.readAvailableByteBuffer(100);
    assertNotEquals(null, rcvbytebuf);

    assertEquals(30 + 1, rcvbytebuf.remaining());
    int len = rcvbytebuf.remaining();
    System.out.println("\n----------------");
    for (int i = 1; i < len - 1; i++) {
      assertEquals(bytebuf.get(i), rcvbytebuf.get(i + 1));
      System.out.printf("%2x,", rcvbytebuf.get(i));
    }
    System.out.println("\n----------------");
    response.releaseBuffer();

    ServerMessageCallback serverCallback = new ServerMessageCallback();
    UdpTransferImpl udpServer = UdpTransferImpl.generateServer(SERVERPORT + 1, serverCallback);
    udpServer.releaseResource();
    udpClient.releaseResource();

  }

  public void testUdpClientServerssShortResponse() throws InterruptedException {
    ByteBuffer bytebuf = ByteBuffer.allocate(30);
    for (int i = 0; i < 30; i++) {
      bytebuf.put((byte) ('a' + i));
    }

    UdpTransferImpl udpClient = UdpTransferImpl.generateClient(3000);
    EchoFutureMessage response = udpClient
        .exchangeMessage(bytebuf, new EndPoint("127.0.0.1", SERVERPORT + 1));

    ByteBuffer rcvbytebuf = response.readAvailableByteBuffer(100);
    assertNotEquals(null, rcvbytebuf);

    assertEquals(30 - 1, rcvbytebuf.remaining());
    int len = rcvbytebuf.remaining();
    System.out.println("\n----------------");
    for (int i = 1; i < len - 1; i++) {
      assertEquals(bytebuf.get(i), rcvbytebuf.get(i + 1));
      System.out.printf("%2x,", rcvbytebuf.get(i));
    }
    System.out.println("\n----------------");

    ServerMessageCallShortLen timeoutCallback = new ServerMessageCallShortLen();
    UdpTransferImpl udpServer = UdpTransferImpl.generateServer(SERVERPORT + 1, timeoutCallback);
    udpServer.releaseResource();
    udpClient.releaseResource();

  }

  public void testUdpClientServerPacketByteArray() throws InterruptedException {
    ByteBuffer bytebuf = ByteBuffer.allocate(30);
    for (int i = 0; i < 30; i++) {
      bytebuf.put((byte) ('a' + i));
    }

    UdpTransferImpl udpClient = UdpTransferImpl.generateClient(3000);
    EchoFutureMessage response = udpClient
        .exchangeMessage(bytebuf, new EndPoint("127.0.0.1", SERVERPORT + 2));

    byte[] rcvbytearray = response.readAvailableByteArray(100);
    assertNotEquals(null, rcvbytearray);

    assertEquals(30 + 1, rcvbytearray.length);
    int len = rcvbytearray.length;
    System.out.println("\n----------------");
    for (int i = 1; i < len - 1; i++) {
      assertEquals(bytebuf.get(i - 1), rcvbytearray[i]);
      System.out.printf("%2x,", rcvbytearray[i + 1]);
    }
    System.out.println("\n----------------");

    ServerMessageCallback timeoutCallback = new ServerMessageCallback();
    UdpTransferImpl udpServer = UdpTransferImpl.generateServer(SERVERPORT + 2, timeoutCallback);
    udpServer.releaseResource();
    udpClient.releaseResource();

  }

  public void testUdpClientServerPerformace() throws InterruptedException {
    final int threads = 100;

    long longtime1 = new Date().getTime();
    System.out.printf("---------------start time = %d-----------------\n", longtime1);
    latch = new CountDownLatch(threads);

    UdpTransferImpl udpClient = UdpTransferImpl.generateClient(2000);
    for (int i = 0; i < threads; i++) {
      Thread t = new Thread(
          new UdpClientTask(udpClient, new EndPoint("127.0.0.1", SERVERPORT + 5), 100, 10000));
      t.start();
    }
    latch.await();

    long longtime2 = new Date().getTime();
    System.out.printf("-----------end time = %d %d %d-------------\n", longtime1, longtime2,
        longtime2 - longtime1);

    UdpTransferImpl udpServer = UdpTransferImpl
        .generateServer(SERVERPORT + 5, new ServerMessageCallback());
    udpServer.releaseResource();
    udpClient.releaseResource();
  }

  private class UdpClientTask implements Runnable {
    private UdpTransferImpl udpClient;
    private EndPoint destPoint;
    private int datalen = 100;
    private int count;

    public UdpClientTask(UdpTransferImpl udptransfer, EndPoint destpoint, int datalen, int count) {
      udpClient = udptransfer;
      destPoint = destpoint;
      this.datalen = datalen;
      this.count = count;
    }

    @Override
    public void run() {
      System.out.printf("-------------- datalen=%d runtimes=%d----------------\n", datalen, count);
      byte[] bytedata = new byte[datalen];

      for (int i = 0; i < datalen; i++) {
        bytedata[i] = (byte) (i % 20 + '1');

      }
      ByteBuffer bytebuf = ByteBuffer.wrap(bytedata);
      for (int j = 0; j < count; j++) {
        for (int i = 0; i < datalen; i++) {
          bytedata[i] = (byte) (i % 20 + '1');

        }

        EchoFutureMessage response = udpClient.exchangeMessage(bytebuf, destPoint);
        boolean isArrived = response.isArrived();

        ByteBuffer rcvbytebuf = response.readAvailableByteBuffer(1000);
        if (rcvbytebuf != null) {
          int len = rcvbytebuf.remaining();

          for (int i = 0; i < len; i++) {
          }

        } else {
          System.out.println("no data received: time out maybe :");
        }
      }
      latch.countDown();
      System.out.printf("countdown=%d \n", latch.getCount());
    }
  }

  private class ServerMessageCallback implements ExchangeMessageCallback {
    @Override
    public void onRecieved(ExchangeMessage message) {
      if (message != null) {
        ByteBuffer bf = message.getPayloadByteBuffer();
        byte[] recvPayload = bf.array();
        int length = recvPayload.length;
        byte[] sendPayload = new byte[length + 1];

        System.arraycopy(recvPayload, 0, sendPayload, 1, length);
        sendPayload[0] = '.';

        message.respond(sendPayload);
      }
    }
  }

  private class ServerMessageCallShortLen implements ExchangeMessageCallback {
    @Override
    public void onRecieved(ExchangeMessage message) {
      if (message != null) {
        ByteBuffer bf = message.getPayloadByteBuffer();
        byte[] recvPayload = bf.array();

        int length = recvPayload.length - 1;
        byte[] sendPayload = new byte[length];

        System.arraycopy(recvPayload, 0, sendPayload, 1, length - 1);
        sendPayload[0] = '.';
        message.respond(sendPayload);
      }
    }
  }

  public class ServerMessageCallbackTimeout implements ExchangeMessageCallback {
    private int delayTimeout;
    private boolean response;

    public ServerMessageCallbackTimeout(int delay, boolean response) {
      this.delayTimeout = delay;
      this.response = response;
    }

    public void setTimeout(int delay) {
      this.delayTimeout = delay;
    }

    public void setResponse(boolean response) {
      this.response = response;
    }

    @Override
    public void onRecieved(ExchangeMessage message) {
      if (message != null) {
        int length = message.getPayloadLength();
        System.out
            .printf("ServerMessageCallbackTimeout received sequnce = %d\n", message.getSequnceId());

        byte[] recvPayload = message.getPayloadByteArray();
        byte[] sendPayload = new byte[length + 1];

        System.arraycopy(recvPayload, 0, sendPayload, 1, length);
        sendPayload[0] = '.';

        System.out
            .printf("ServerMessageCallbackTimeout sequcne %d sleep = %d\n", message.getSequnceId(),
                delayTimeout);
        try {
          Thread.sleep(delayTimeout);
        } catch (InterruptedException e) {
          logger.error("caught exception", e);

        }

        if (response) {
          message.respond(sendPayload);
        }
      }
    }
  }
}

