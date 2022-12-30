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

package py.udp;

import static org.junit.Assert.assertEquals;

import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Level;
import org.junit.Test;
import py.common.struct.EndPoint;
import py.connection.pool.udp.NioUdpEchoServer;
import py.connection.pool.udp.detection.DetectionTimeoutPolicy;
import py.connection.pool.udp.detection.UdpDetector;
import py.connection.pool.udp.detection.UdpDetectorImpl;
import py.test.TestBase;

public class UdpDetectManagerTest extends TestBase {
  private final int testCnt = 100;

  private final int testMaxNum = 10000;

  private EndPoint[] endPoint = new EndPoint[testMaxNum];
  private NioUdpEchoServer[] udpServer = new NioUdpEchoServer[testMaxNum];

  private UdpDetector getUdpDetector() throws SocketException {
   
    return UdpDetectorImpl.INSTANCE.start(0, () -> new DetectionTimeoutPolicy() {
      long start = System.currentTimeMillis();

      @Override
      public long getTimeoutMs() {
        if (System.currentTimeMillis() - start > 500) {
          return -1;
        } else {
          return 10;
        }
      }

    });
  }

  @Test
  public void detectOneClintToOneServerSuccess() throws Exception {
    setLogLevel(Level.DEBUG);
    logger.warn("======detectOneClintToOneServerSuccess begin.====");

    int instanceNum = this.testCnt;

    for (int index = 0; index < instanceNum; index++) {
      endPoint[index] = new EndPoint("localhost", 25453 + index * 2);
      udpServer[index] = new NioUdpEchoServer(endPoint[index]);
      udpServer[index].startEchoServer();
      Thread.sleep(10);
    }

    UdpDetectListenerTest udpDetectListener = new UdpDetectListenerTest();
    UdpDetector udpDetectManager = getUdpDetector();

    long connectBegin = System.currentTimeMillis();
    for (int index = 0; index < instanceNum; index++) {
      udpDetectManager.detect(endPoint[index]).thenAccept(udpDetectListener::complete);
      Thread.sleep(10);
    }

    long connectEnd = System.currentTimeMillis();

    logger.warn("It's cost {} ms, from connect begin to end.", connectEnd - connectBegin);

    while ((udpDetectListener.getConnectSuccess().intValue() + udpDetectListener.getConnectFail()
        .intValue()) < instanceNum) {
      Thread.sleep(100);
    }

    logger.warn("The test result is success: {}, failed {} UDP manager as {}",
        udpDetectListener.getConnectSuccess().intValue(),
        udpDetectListener.getConnectFail().intValue(), udpDetectManager);

    for (int index = 0; index < instanceNum; index++) {
      udpServer[index].stopEchoServer();
    }

    Validate.isTrue(udpDetectListener.getConnectFail().intValue() == 0);
    Validate.isTrue(udpDetectListener.getConnectSuccess().intValue() == instanceNum);

    logger.warn("======detectOneClintToOneServerSuccess finished.====");

    Thread.sleep(200);
  }

  @Test
  public void detectMoreClintToOneServerSuccess() throws Exception {
    logger.warn("======detectMoreClintToOneServerSuccess begin.====");

    int instanceNum = this.testCnt;

    endPoint[0] = new EndPoint("localhost", 25453);

    Thread serverThread = new Thread("udp-detect-server") {
      public void run() {
        try {
          udpServer[0] = new NioUdpEchoServer(endPoint[0]);
          udpServer[0].startEchoServer();
        } catch (Exception e) {
          logger.error("Thread {} start UDP detect failed.", this.getName());
        }

      }
    };
    serverThread.start();

    UdpDetector udpDetectManager = getUdpDetector();

    UdpDetectListenerTest udpDetectListener = new UdpDetectListenerTest();

    long connectBegin = System.currentTimeMillis();

    for (int index = 0; index < instanceNum; index++) {
      int clintIndex = index;

      Thread clintThread = new Thread("udp-detect-clint") {
        public void run() {
          try {
            udpDetectManager.detect(endPoint[0]).thenAccept(udpDetectListener::complete);
          } catch (Exception e) {
            logger.error("Thread {} start UDP detect failed.", this.getName());
          }
        }
      };

      clintThread.start();
    }

    long connectEnd = System.currentTimeMillis();
    logger.warn("It's cost {} ms, from connect begin to end.", connectEnd - connectBegin);

    while ((udpDetectListener.getConnectSuccess().intValue() + udpDetectListener.getConnectFail()
        .intValue()) < instanceNum) {
      Thread.sleep(100);
    }

    udpServer[0].stopEchoServer();

    logger.warn("The test result is success: {}, failed {}",
        udpDetectListener.getConnectSuccess().intValue(),
        udpDetectListener.getConnectFail().intValue());
    Validate.isTrue(udpDetectListener.getConnectFail().intValue() == 0);
    Validate.isTrue(udpDetectListener.getConnectSuccess().intValue() == instanceNum);

    logger.warn("======detectMoreClintToOneServerSuccess finished.====");

    Thread.sleep(200);
  }

  @Test
  public void detectOneClintToMoreServerSuccess() throws Exception {
    logger.warn("======detectOneClintToMoreServerSuccess begin.====");

    int instanceNum = this.testCnt;

    for (int index = 0; index < instanceNum; index++) {
      endPoint[index] = new EndPoint("localhost", 25453 + index * 2);

      udpServer[index] = new NioUdpEchoServer(endPoint[index]);
    }

    for (int index = 0; index < instanceNum; index++) {
      int serverIndex = index;

      Thread serverThread = new Thread("udp-detect-server" + index) {
        public void run() {
          try {
            udpServer[serverIndex].startEchoServer();
          } catch (Exception e) {
            logger.error("Thread {} start UDP detect failed.", this.getName());
          }
        }
      };

      serverThread.start();
      Thread.sleep(20);
    }

    Thread.sleep(1000);

    UdpDetector udpDetectManager = getUdpDetector();

    UdpDetectListenerTest udpDetectListener = new UdpDetectListenerTest();

    long connectBegin = System.currentTimeMillis();
    for (int index = 0; index < instanceNum; index++) {
      try {
        udpDetectManager.detect(endPoint[index]).thenAccept(udpDetectListener::complete);
        Thread.sleep(10);

      } catch (Exception e) {
        logger.error("endPoint {} start UDP detect failed.", endPoint[index]);
      }
    }

    long connectEnd = System.currentTimeMillis();
    logger.warn("It's cost {} ms, from connect begin to end.", connectEnd - connectBegin);

    while ((udpDetectListener.getConnectSuccess().intValue() + udpDetectListener.getConnectFail()
        .intValue()) < instanceNum) {
      Thread.sleep(100);
    }

    for (int index = 0; index < instanceNum; index++) {
      udpServer[index].stopEchoServer();
    }

    logger.warn("The test result is success: {}, failed {}",
        udpDetectListener.getConnectSuccess().intValue(),
        udpDetectListener.getConnectFail().intValue());
    Validate.isTrue(udpDetectListener.getConnectFail().intValue() == 0);
    Validate.isTrue(udpDetectListener.getConnectSuccess().intValue() == instanceNum);

    logger.warn("======detectOneClintToMoreServerSuccess finished.====");

    Thread.sleep(200);
  }

  @Test
  public void detectServerNoReply() throws Exception {
    logger.warn("======detectServerNoReply begin.====");

    int instanceNum = this.testCnt;

    for (int index = 0; index < instanceNum; index++) {
      endPoint[index] = new EndPoint("localhost", 25453 + index * 2);
    }

    UdpDetector udpDetectManager = getUdpDetector();

    UdpDetectListenerTest udpDetectListener = new UdpDetectListenerTest();

    long connectBegin = System.currentTimeMillis();
    for (int index = 0; index < instanceNum; index++) {
      udpDetectManager.detect(endPoint[index]).thenAccept(udpDetectListener::complete);
    }
    long connectEnd = System.currentTimeMillis();
    logger.warn("It's cost {} ms, from connect begin to end.", connectEnd - connectBegin);

    while ((udpDetectListener.getConnectSuccess().intValue() + udpDetectListener.getConnectFail()
        .intValue()) < instanceNum) {
      Thread.sleep(100);
    }

    logger.warn("The test result is success {}, failed {}",
        udpDetectListener.getConnectSuccess().intValue(),
        udpDetectListener.getConnectFail().intValue());

    Validate.isTrue(udpDetectListener.getConnectFail().intValue() == instanceNum);
    Validate.isTrue(udpDetectListener.getConnectSuccess().intValue() == 0);

    logger.warn("======detectServerNoReply finished.====");

    Thread.sleep(200);
  }

  @Test
  public void detectServerNotExistIp() throws Exception {
    logger.warn("======detectServerNotExistIP begin.====");

    int instanceNum = this.testCnt;

    for (int index = 0; index < instanceNum; index++) {
      endPoint[index] = new EndPoint("197.168.10.1", 25453 + index * 2);
    }

    UdpDetector udpDetectManager = getUdpDetector();

    UdpDetectListenerTest udpDetectListener = new UdpDetectListenerTest();

    long connectBegin = System.currentTimeMillis();

    for (int index = 0; index < instanceNum; index++) {
      udpDetectManager.detect(endPoint[index]).thenAccept(udpDetectListener::complete);
    }
    long connectEnd = System.currentTimeMillis();
    logger.warn("It's cost {} ms, from connect begin to end.", connectEnd - connectBegin);

    while ((udpDetectListener.getConnectSuccess().intValue() + udpDetectListener.getConnectFail()
        .intValue()) < instanceNum) {
      Thread.sleep(100);
    }

    logger.warn("The test result is success: {}, failed {}",
        udpDetectListener.getConnectSuccess().intValue(),
        udpDetectListener.getConnectFail().intValue());

    Validate.isTrue(udpDetectListener.getConnectFail().intValue() == instanceNum);
    Validate.isTrue(udpDetectListener.getConnectSuccess().intValue() == 0);

    logger.warn("======detectServerNotExistIP finished.====");

    Thread.sleep(200);
  }

  @Test
  public void detectServerNotPointHost() throws Exception {
    logger.warn("======detectServerNotPointHost begin.====");

    int instanceNum = this.testCnt;
    EndPoint[] serverEndPoint = new EndPoint[instanceNum];

    for (int index = 0; index < instanceNum; index++) {
      endPoint[index] = new EndPoint("localhost", 25453 + index * 2);

      serverEndPoint[index] = new EndPoint();
      serverEndPoint[index].setPort(25453 + index * 2);
      udpServer[index] = new NioUdpEchoServer(serverEndPoint[index]);
      udpServer[index].startEchoServer();

      Thread.sleep(10);
    }

    UdpDetector udpDetectManager = getUdpDetector();

    UdpDetectListenerTest udpDetectListener = new UdpDetectListenerTest();

    long connectBegin = System.currentTimeMillis();
    for (int index = 0; index < instanceNum; index++) {
      udpDetectManager.detect(endPoint[index]).thenAccept(udpDetectListener::complete);
      Thread.sleep(10);
    }

    long connectEnd = System.currentTimeMillis();

    logger.warn("It's cost {} ms, from connect begin to end.", connectEnd - connectBegin);

    while ((udpDetectListener.getConnectSuccess().intValue() + udpDetectListener.getConnectFail()
        .intValue()) < instanceNum) {
      Thread.sleep(100);
    }

    logger.warn("The test result is success: {}, failed {}",
        udpDetectListener.getConnectSuccess().intValue(),
        udpDetectListener.getConnectFail().intValue());

    for (int index = 0; index < instanceNum; index++) {
      udpServer[index].stopEchoServer();
    }

    assertEquals(0, udpDetectListener.getConnectFail().intValue());
    assertEquals(instanceNum, udpDetectListener.getConnectSuccess().intValue());

    logger.warn("======detectServerNotPointHost finished.====");

    Thread.sleep(200);
  }

  @Test
  public void detectMoreClintToMoreServerByPressParallel() throws Exception {
    logger.warn("======detectMoreClintToMoreServerByPressParallel begin.====");
   
    int clintNum = 100;
   
    int serverNum = 10;

    for (int index = 0; index < serverNum; index++) {
      endPoint[index] = new EndPoint("localhost", 25453 + index * 2);
      udpServer[index] = new NioUdpEchoServer(endPoint[index]);
    }

    for (int index = 0; index < serverNum; index++) {
      int serverIndex = index;
      try {
        udpServer[serverIndex].startEchoServer();
      } catch (Exception e) {
        logger.error("Thread {} start UDP detect failed.");
      }
      Thread.sleep(200);
    }

    UdpDetector udpDetectManager = getUdpDetector();

    UdpDetectListenerTest udpDetectListener = new UdpDetectListenerTest();

    long connectBegin = System.currentTimeMillis();

    Random indexRandom = new Random();

    int threadCount = serverNum;
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch stopLatch = new CountDownLatch(threadCount);
    for (int i = 0; i < threadCount; i++) {
      Thread clintThread = new Thread("udp-detect-clint-" + i) {
        public void run() {
          try {
            startLatch.await();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

          for (int index = 0; index < clintNum; index++) {
            int serverRandIndex = indexRandom.nextInt(serverNum);
            try {
              udpDetectManager.detect(endPoint[serverRandIndex])
                  .thenAccept(udpDetectListener::complete);
            } catch (Exception e) {
              logger.error("Thread {} start UDP detect failed.", this.getName());
            }

          }

          stopLatch.countDown();
        }
      };

      clintThread.start();
    }

    startLatch.countDown();
    long connectEnd = System.currentTimeMillis();
    logger.warn("It's cost {} ms, from connect begin to end.", connectEnd - connectBegin);

    stopLatch.await();

    while ((udpDetectListener.getConnectSuccess().intValue() + udpDetectListener.getConnectFail()
        .intValue()) < clintNum * threadCount) {
      Thread.sleep(100);
    }

    for (int index = 0; index < serverNum; index++) {
      udpServer[index].stopEchoServer();
    }

    logger.warn("The test result is success: {}, failed {}",
        udpDetectListener.getConnectSuccess().intValue(),
        udpDetectListener.getConnectFail().intValue());
    assertEquals(0, udpDetectListener.getConnectFail().intValue());
    assertEquals(clintNum * threadCount, udpDetectListener.getConnectSuccess().intValue());

    logger.warn("======detectMoreClintToMoreServerByPressParallel finished.====");

    Thread.sleep(800);
  }

  @Test
  public void detectMoreClintToMoreServerByPressMoreThreads() throws Exception {
    logger.warn("======detectMoreClintToMoreServerByPressParallel begin.====");

    int clintNum = 100;
   
    int serverNum = 10;

    for (int index = 0; index < serverNum; index++) {
      endPoint[index] = new EndPoint("localhost", 25453 + index * 2);
      udpServer[index] = new NioUdpEchoServer(endPoint[index]);
      Thread.sleep(10);
    }

    for (int index = 0; index < serverNum; index++) {
      int serverIndex = index;
      Thread serverThread = new Thread("udp-detect-server" + index) {
        public void run() {
          try {
            udpServer[serverIndex].startEchoServer();
          } catch (Exception e) {
            logger.error("Thread {} start UDP detect failed.", this.getName());
          }
        }
      };
      serverThread.start();
    }

    UdpDetector udpDetectManager = getUdpDetector();

    UdpDetectListenerTest udpDetectListener = new UdpDetectListenerTest();

    long connectBegin = System.currentTimeMillis();

    Random indexRandom = new Random();

    for (int index = 0; index < clintNum; index++) {
      int clintIndex = index;
      int serverRandIndex = indexRandom.nextInt(serverNum);

      Thread clintThread = new Thread("udp-detect-clint" + index) {
        public void run() {
          try {
            udpDetectManager.detect(endPoint[serverRandIndex])
                .thenAccept(udpDetectListener::complete);
          } catch (Exception e) {
            logger.error("Thread {} start UDP detect failed.", this.getName());
          }
        }
      };

      clintThread.start();
      Thread.sleep(20);
    }

    long connectEnd = System.currentTimeMillis();
    logger.warn("It's cost {} ms, from connect begin to end.", connectEnd - connectBegin);

    while ((udpDetectListener.getConnectSuccess().intValue() + udpDetectListener.getConnectFail()
        .intValue()) < clintNum) {
      Thread.sleep(100);
    }

    for (int index = 0; index < serverNum; index++) {
      udpServer[index].stopEchoServer();
    }

    logger.warn("The test result is success: {}, failed {}",
        udpDetectListener.getConnectSuccess().intValue(),
        udpDetectListener.getConnectFail().intValue());
    Validate.isTrue(udpDetectListener.getConnectFail().intValue() == 0);
    Validate.isTrue(udpDetectListener.getConnectSuccess().intValue() == clintNum);

    logger.warn("======detectMoreClintToMoreServerByPressParallel finished.====");

    Thread.sleep(800);
  }

  public void detectOutWhenIoResponseRechable() throws Exception {
    logger.warn("======detectOutWhenIOResponseRechable begin.====");

    endPoint[0] = new EndPoint("localhost", 25453);

    UdpDetector udpDetectManager = getUdpDetector();

    UdpDetectListenerTest udpDetectListener = new UdpDetectListenerTest();

    try {
      udpDetectManager.detect(endPoint[0]).thenAccept(udpDetectListener::complete);
    } catch (Exception e) {
      logger.error("endPoint {} start UDP detect failed.", endPoint[0]);
    }

    while (udpDetectListener.getConnectFail().intValue() + udpDetectListener.getConnectSuccess()
        .intValue() < 1) {
      Thread.sleep(10);
    }

    long connectEnd = System.currentTimeMillis();

    logger.warn("The test result is success: {}, failed {}",
        udpDetectListener.getConnectSuccess().intValue(),
        udpDetectListener.getConnectFail().intValue());

    logger.warn("======detectOutWhenIOResponseRechable finished.====");

    Thread.sleep(200);
  }

  public void detectWhenOneServerToDown() throws Exception {
    logger.warn("======detectOneClintToMoreServerSuccess begin.====");

    int instanceNum = this.testCnt;

    for (int index = 0; index < instanceNum; index++) {
      endPoint[index] = new EndPoint("localhost", 25453 + index * 2);

      udpServer[index] = new NioUdpEchoServer(endPoint[index]);
    }

    for (int index = 0; index < instanceNum; index++) {
      int serverIndex = index;

      try {
        udpServer[serverIndex].startEchoServer();
      } catch (Exception e) {
        logger.error("Thread {} start UDP detect failed.");
      }

      Thread.sleep(20);
    }

    Thread.sleep(1000);

    UdpDetector udpDetectManager = getUdpDetector();

    UdpDetectListenerTest udpDetectListener = new UdpDetectListenerTest();

    for (int index = 0; index < instanceNum; index++) {
      try {
        udpDetectManager.detect(endPoint[index]).thenAccept(udpDetectListener::complete);
        Thread.sleep(10);

      } catch (Exception e) {
        logger.error("endPoint {} start UDP detect failed.", endPoint[index]);
      }
    }

    for (int index = instanceNum - 5; index < instanceNum; index++) {
      udpServer[index].stopEchoServer();
    }

    for (int index = instanceNum - 5; index < instanceNum; index++) {
      try {
        udpDetectManager.detect(endPoint[index]).thenAccept(udpDetectListener::complete);
        Thread.sleep(10);
      } catch (Exception e) {
        logger.error("endPoint {} start UDP detect failed.", endPoint[index]);
      }
    }

    long connectEnd = System.currentTimeMillis();
   
    long connectBegin = System.currentTimeMillis();
    logger.warn("It's cost {} ms, from connect begin to end.", connectEnd - connectBegin);

    Thread.sleep(10000);
   

    for (int index = 0; index < instanceNum - 5; index++) {
      udpServer[index].stopEchoServer();
    }

    logger.warn("The test result is success: {}, failed {}",
        udpDetectListener.getConnectSuccess().intValue(),
        udpDetectListener.getConnectFail().intValue());
    Validate.isTrue(udpDetectListener.getConnectFail().intValue() == 5);
    Validate.isTrue(udpDetectListener.getConnectSuccess().intValue() == instanceNum);

    logger.warn("======detectOneClintToMoreServerSuccess finished.====");

    Thread.sleep(200);
  }

  class UdpDetectListenerTest {
    private AtomicInteger connectSuccess;
    private AtomicInteger connectFail;

    public UdpDetectListenerTest() {
      this.connectSuccess = new AtomicInteger(0);
      this.connectFail = new AtomicInteger(0);
    }

    public AtomicInteger getConnectSuccess() {
      return connectSuccess;
    }

    public AtomicInteger getConnectFail() {
      return connectFail;
    }

    public void complete(boolean result) {
      if (result) {
        connectSuccess.incrementAndGet();
        logger.warn("The UDP detect successfully!!");
      } else {
        connectFail.incrementAndGet();
        logger.warn("The UDP detect failed!!");
      }
    }
  }
}

