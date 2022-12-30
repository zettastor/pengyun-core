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

package py.connection.pool.udp.detection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import py.common.Utils;
import py.common.struct.EndPoint;
import py.connection.pool.udp.NioUdpEchoServer;
import py.test.TestBase;

public class UdpDetectorImplTest extends TestBase {
  static final int MAX_RETRY_TIMES = 7;
  static final int INIT_TIMEOUT_MS = 20;

  static final int DEFAULT_PORT = 25453;

  int serverCount = 100;

  EndPoint[] endPoints = new EndPoint[serverCount];
  NioUdpEchoServer[] udpServers = new NioUdpEchoServer[serverCount];
  UdpDetector detector;

  @Before
  public void initUdpServerAndDetector() throws Exception {
    for (int i = 0; i < serverCount; i++) {
      endPoints[i] = new EndPoint("localhost", DEFAULT_PORT + i);
      udpServers[i] = new NioUdpEchoServer(endPoints[i]);
      udpServers[i].startEchoServer();
      Thread.sleep(10);
    }
    detector = UdpDetectorImpl.INSTANCE
        .start(DEFAULT_PORT,
            new DefaultDetectionTimeoutPolicyFactory(MAX_RETRY_TIMES, INIT_TIMEOUT_MS));
  }

  @After
  public void after() {
    for (int i = 0; i < serverCount; i++) {
      udpServers[i].stopEchoServer();
    }
  }

  @Test
  public void basicTest() throws TimeoutException {
    AtomicInteger successCount = new AtomicInteger();
    AtomicInteger failedCount = new AtomicInteger();

    for (EndPoint endPoint : endPoints) {
      detector.detect(endPoint).thenAccept(result -> {
        if (result) {
          successCount.incrementAndGet();
        } else {
          failedCount.incrementAndGet();
        }
      });
    }

    Utils.waitUntilConditionMatches(1, () -> successCount.get() == serverCount);

    assertEquals(successCount.get(), serverCount);
    assertEquals(failedCount.get(), 0);

    udpServers[0].stopEchoServer();

    int count = 10000;
    for (int i = 0; i < count; i++) {
      new Thread(() -> detector.detect(endPoints[0]).thenAccept(result -> {
        if (result) {
          successCount.incrementAndGet();
        } else {
          failedCount.incrementAndGet();
        }
      })).start();
    }

    Utils.waitUntilConditionMatches(2, () -> failedCount.get() == count);

  }

  long getTimeout() {
    DetectionTimeoutPolicy policy = new DefaultDetectionTimeoutPolicyFactory(MAX_RETRY_TIMES,
        INIT_TIMEOUT_MS).generate();

    long timeout = 0;

    while (true) {
      long newTimeout = policy.getTimeoutMs();
      if (newTimeout < 0) {
        break;
      }
      timeout += newTimeout;
    }

    return timeout;
  }

  @Test
  public void testTimeout() throws ExecutionException, InterruptedException {
    udpServers[0].stopEchoServer();

    long timeout = getTimeout();

    CompletableFuture<Boolean> future = detector.detect(endPoints[0]);

    long startTime = System.currentTimeMillis();
    assertFalse(future.get());
    long cost = System.currentTimeMillis() - startTime;

    logger.warn("timeout={}, cost={}. they should be very close", timeout, cost);

    assertTrue(Math.abs(timeout - cost) < 50);
  }

  @Test
  public void testOnlyOnePacketSent() throws ExecutionException, InterruptedException {
    int concurrent = 10;
    int testCount = 10;

    AtomicInteger receiveCount = new AtomicInteger(0);

    udpServers[0].setSendBack(false);
    udpServers[0].setReceiveListener(b -> receiveCount.incrementAndGet());

    for (int i = 0; i < testCount; i++) {
      for (int j = 0; j < concurrent; j++) {
        new Thread(() -> detector.detect(endPoints[0])).start();
      }
      assertFalse(detector.detect(endPoints[0]).get());
    }

    assertEquals(testCount * MAX_RETRY_TIMES, receiveCount.get());
  }

}