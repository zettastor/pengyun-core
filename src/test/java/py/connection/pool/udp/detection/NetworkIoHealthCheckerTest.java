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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class NetworkIoHealthCheckerTest extends UdpDetectorImplTest {
  private final NetworkIoHealthChecker checker = NetworkIoHealthChecker.INSTANCE;

  @Test
  public void testMarkingWillStartTask() throws InterruptedException {
    checker.start(detector);

    udpServers[0].setSendBack(false);

    String localhost = "localhost";

    long timeout = getTimeout();

    assertFalse(checker.isUnreachable(localhost));

    Thread.sleep(NetworkIoHealthChecker.INTERVAL_MS + timeout + 100);
    assertFalse(checker.isUnreachable(localhost));

    checker.markReachableAndKeepChecking(localhost);
    assertFalse(checker.isUnreachable(localhost));

    Thread.sleep(NetworkIoHealthChecker.INTERVAL_MS + timeout + 100);
    assertTrue(checker.isUnreachable(localhost));
  }

  @Test
  public void testReachableOrNot() throws InterruptedException, ExecutionException {
    checker.start(detector);

    String ip = "192.168.1.1";

    assertFalse(checker.isUnreachable(ip));

    long timeout = getTimeout();

    long start = System.currentTimeMillis();
    boolean result = checker.blockingCheck(ip);
    long cost = System.currentTimeMillis() - start;

    assertTrue(Math.abs(cost - timeout) < 50);
    assertFalse(result);

    assertTrue(checker.isUnreachable(ip));

    String localhost = "localhost";

    AtomicInteger receiveCount = new AtomicInteger(0);
    udpServers[0].setReceiveListener(b -> receiveCount.incrementAndGet());

    assertFalse(checker.isUnreachable(localhost));

    start = System.currentTimeMillis();
    result = checker.blockingCheck(localhost);
    cost = System.currentTimeMillis() - start;

    assertTrue(cost < 50);
    assertTrue(result);
    assertFalse(checker.isUnreachable(localhost));
    assertEquals(1, receiveCount.get());

    udpServers[0].setSendBack(false);

    assertFalse(checker.isUnreachable(localhost));

    Thread.sleep(NetworkIoHealthChecker.INTERVAL_MS);

    assertFalse(checker.isUnreachable(localhost));

    Thread.sleep(timeout + 100);

    assertTrue(checker.isUnreachable(localhost));

    udpServers[0].setSendBack(true);
    assertTrue(checker.isUnreachable(localhost));

    Thread.sleep(NetworkIoHealthChecker.INTERVAL_MS + timeout + 100);

    assertFalse(checker.isUnreachable(localhost));

    udpServers[0].setSendBack(false);

    assertFalse(checker.blockingCheck(localhost));

    checker.markReachableAndKeepChecking(localhost).get();
    assertFalse(checker.isUnreachable(localhost));

    Thread.sleep(NetworkIoHealthChecker.INTERVAL_MS + timeout + 100);
    assertTrue(checker.isUnreachable(localhost));
  }

}