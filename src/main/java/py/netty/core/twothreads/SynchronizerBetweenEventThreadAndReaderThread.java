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

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynchronizerBetweenEventThreadAndReaderThread {
  private static final Logger logger = LoggerFactory
      .getLogger(SynchronizerBetweenEventThreadAndReaderThread.class);
  final AtomicReference<MyCyclicBarrier> barrierAtomicReference;
  private volatile boolean clientWakingServerUp = false;
  private Selector selector;

  public SynchronizerBetweenEventThreadAndReaderThread(int seqId, Selector selector)
      throws IOException {
    barrierAtomicReference = new AtomicReference<>(new MyCyclicBarrier(2));
    this.selector = selector;
  }

  public void register(Selector selector) {
    logger.warn("register a new selector {} ", selector);
    this.selector = selector;
  }

  public void serverWaitOnBarrier() {
    if (clientWakingServerUp) {
      logger.warn("waken up by the client");
      clientWakingServerUp = false;
      CyclicBarrier barrier = barrierAtomicReference.get();
      try {
        if (barrier != null) {
          barrier.await();
          logger.warn("server finished waiting on the barrier");
        } else {
          logger.warn("barrier is null while client might be waiting on the barrier");
        }
      } catch (Exception e) {
        logger.warn("the server side interrupted while waiting for the barrier", e);
      }
    }
  }

  public void clientWakeupServer() {
    clientWakingServerUp = true;
    selector.wakeup();
  }

  public void clientWaitOnBarrier() {
    CyclicBarrier barrier = barrierAtomicReference.get();
    try {
      if (barrier != null) {
        logger.warn("client starts to wait on barrier");
        barrier.await();
        logger.warn("client finished waiting on barrier");
      }
    } catch (Exception e) {
      logger.warn("the server side interrupted while waiting for the barrier");
    }
  }

  public void close() {
    logger.debug("closing synchronizer");
    CyclicBarrier barrier = barrierAtomicReference.getAndUpdate(oldValue -> null);
    if (barrier != null) {
      barrier.reset();
    }
  }

  protected static class MyCyclicBarrier extends CyclicBarrier {
    private final AtomicBoolean hasReset = new AtomicBoolean(false);

    public MyCyclicBarrier(int parties) {
      super(parties);
    }

    @Override
    public void reset() {
      hasReset.set(true);
      super.reset();
    }

    @Override
    public int await() throws InterruptedException, BrokenBarrierException {
      if (!hasReset.get()) {
        return super.await();
      } else {
        return 0;
      }
    }

    @Override
    public int await(long timeout, TimeUnit unit)
        throws InterruptedException, BrokenBarrierException, TimeoutException {
      if (!hasReset.get()) {
        return super.await(timeout, unit);
      } else {
        return 0;
      }
    }
  }
}
