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

package py.netty.core.twothreads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import py.test.TestBase;

public class SynchronizerBetweenEventThreadAndReaderThreadTest extends TestBase {
  @Test
  public void testMyCyclicBarrier() throws Exception {
    SynchronizerBetweenEventThreadAndReaderThread.MyCyclicBarrier barrier
        = new SynchronizerBetweenEventThreadAndReaderThread.MyCyclicBarrier(
        2);

    barrier.reset();

    assertEquals(0, barrier.await());
    assertEquals(0, barrier.await(10000, TimeUnit.SECONDS));
  }

  @Test
  public void basicTest() throws Exception {
    SelectorProvider provider = SelectorProvider.provider();
    Selector selector = provider.openSelector();
    SynchronizerBetweenEventThreadAndReaderThread synchronizer
        = new SynchronizerBetweenEventThreadAndReaderThread(
        1, selector);

    final int count = 10000;

    Runnable reader = new Runnable() {
      @Override
      public void run() {
        try {
          for (int nrounds = 0; nrounds < count; nrounds++) {
            int numKeys = selector.select();
            synchronizer.serverWaitOnBarrier();

            logger.debug("count {}", nrounds);
          }
        } catch (IOException e) {
          logger.warn("can't register ", e);
          fail();
        }
      }
    };

    Runnable sender = new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < count; i++) {
          synchronizer.clientWakeupServer();
          logger.debug("poke");
          synchronizer.clientWaitOnBarrier();
          logger.debug("notify peer");
        }
      }
    };

    Thread senderThread = new Thread(sender);
    Thread readerThread = new Thread(reader);

    readerThread.start();
    senderThread.start();

    senderThread.join();
    logger.debug("wait until the reader thread is done ");

    long start = System.nanoTime();
    readerThread.join(2000);
    long end = System.nanoTime();

    long during = (end - start) / 1000;
    logger.debug("it takes {} ms to complete the test", during);
    assertTrue(during < 2000);
    synchronizer.close();
  }
}

