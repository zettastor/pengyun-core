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

package py.common.tlsf.bytebuffer.manager;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.DirectAlignedBufferAllocator;
import py.common.tlsf.OutOfSpaceException;
import py.test.TestBase;

public class TlsfByteBufferManagerTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(TlsfByteBufferManager.class);

  @Override
  public void init() throws Exception {
    super.init();
  }

  @Test
  public void testCheckingAllocationSize() throws Exception {
    final int nTimes = 1000000;
    final int baseSize = 512;
    final int totalSize = 100 * 1024 * 1024;
    final ByteBuffer bogus = Mockito.mock(ByteBuffer.class);

    AtomicBoolean hasErr = new AtomicBoolean(false);
    CountDownLatch latch = new CountDownLatch(2);
    Random random = new Random(System.currentTimeMillis());
    BlockingQueue<ByteBuffer> bufferQue = new LinkedBlockingQueue<>();
    TlsfByteBufferManager spaceManager = new TlsfByteBufferManager(512, totalSize, true);

    Thread allocator = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          for (int times = 0; times < nTimes; times++) {
            int nbases = random.nextInt(512) + 512;
            int space = nbases * baseSize;
            ByteBuffer buffer;

            buffer = spaceManager.blockingAllocate(space);
            bufferQue.offer(buffer);
            if (buffer.remaining() < space) {
              logger.error("remaining: {}, size: {}", buffer.remaining(), space);
              hasErr.set(true);
              break;
            }
          }
        } finally {
          bufferQue.offer(bogus);
          latch.countDown();
        }
      }
    });

    Thread releaser = new Thread(new Runnable() {
      @Override
      public void run() {
        ByteBuffer buffer;

        try {
          while ((buffer = bufferQue.take()) != bogus) {
            spaceManager.release(buffer);
          }
        } catch (Exception e) {
          logger.error("Caught an exception", e);
          hasErr.set(true);
        } finally {
          latch.countDown();
        }
      }
    });

    releaser.start();
    allocator.start();
    latch.await();

    Assert.assertFalse(hasErr.get());
  }

  @Test
  public void testAllocateWholeSpace() throws Exception {
    final int totalSize = 10 * 1024 * 1024;
    final int times = 10;

    TlsfByteBufferManager spaceManager = new TlsfByteBufferManager(4, totalSize, true);

    ByteBuffer buffer;
    for (int i = 0; i < times; i++) {
      buffer = spaceManager.allocate(totalSize);

      try {
        spaceManager.allocate(1);
        Assert.fail();
      } catch (OutOfSpaceException e) {
        logger.error("caught exception", e);
      }

      spaceManager.release(buffer);

      buffer.clear();
      Assert.assertEquals(buffer.capacity(), totalSize);
    }
  }

  @Test
  public void testAllocateMostNumOfDivisions() throws Exception {
    final int totalSize = 10 * 1024 * 1024;

    TlsfByteBufferManager spaceManager = new TlsfByteBufferManager(512, totalSize, true);

    List<ByteBuffer> bufferList = new ArrayList<>();
    while (true) {
      try {
        bufferList.add(spaceManager.allocate(1));
      } catch (OutOfSpaceException e) {
        logger
            .debug("Allocated {} divisions each of which has at least {} bytes", bufferList.size(),
                512);
        break;
      }
    }

    Assert.assertEquals(totalSize / 512, bufferList.size());

    for (ByteBuffer buffer : bufferList) {
      buffer.clear();
      Assert.assertTrue(DirectAlignedBufferAllocator.getAddress(buffer) % 512 == 0);
      Assert.assertEquals(512, buffer.capacity());

      spaceManager.release(buffer);
    }

    ByteBuffer buffer = spaceManager.allocate(totalSize);
    spaceManager.release(buffer);
  }

  @Test
  public void testTryAllocate() throws Exception {
    final int totalSize = 10 * 1024 * 1024;

    TlsfByteBufferManager spaceManager = new TlsfByteBufferManager(512, totalSize, true);

    List<ByteBuffer> bufferList = new LinkedList<>();
    while (true) {
      try {
        bufferList.add(spaceManager.allocate(512));
      } catch (OutOfSpaceException e) {
        logger
            .debug("Allocated {} divisions each of which has at least {} bytes", bufferList.size(),
                512);
        break;
      }
    }

    int index = 0;
    int desiredSize = 0;
    Iterator<ByteBuffer> bufferIterator = bufferList.iterator();
    List<ByteBuffer> remainingSpaceDivisions = new ArrayList<>();
    while (bufferIterator.hasNext()) {
      ByteBuffer buffer = bufferIterator.next();
      if (index % 2 == 0) {
        spaceManager.release(buffer);
        desiredSize += 512;
      } else {
        remainingSpaceDivisions.add(buffer);
      }
      index++;
    }

    try {
      logger.debug("Try allocating {} bytes!", desiredSize);
      spaceManager.allocate(desiredSize);
      Assert.fail();
    } catch (OutOfSpaceException e) {
      logger.error("caught exception", e);
    }

    try {
      bufferList = spaceManager.tryAllocate(desiredSize + 1);
      Assert.fail();
    } catch (OutOfSpaceException e) {
      logger.error("caught exception", e);

    }

    bufferList = spaceManager.tryAllocate(desiredSize);
    logger.debug("There are {} buffers in list, {} buffers in remaining list!", bufferList.size(),
        remainingSpaceDivisions.size());
    for (ByteBuffer buffer : bufferList) {
      spaceManager.release(buffer);
    }

    for (ByteBuffer buffer : remainingSpaceDivisions) {
      spaceManager.release(buffer);
    }

    ByteBuffer buffer = spaceManager.allocate(totalSize);
    spaceManager.release(buffer);
  }

  @Test
  public void testSilentAllocate() throws Exception {
    final int totalSize = 10 * 1024 * 1024;

    final TlsfByteBufferManager spaceManager = new TlsfByteBufferManager(4, totalSize, true);

    final ByteBuffer buffer = spaceManager.allocate(totalSize);

    new Thread() {
      public void run() {
        try {
          logger.debug("Wait 2 sec for releasing buffer ...");
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          logger.error("caught exception", e);
        }

        spaceManager.release(buffer);
      }
    }.start();

    spaceManager.blockingAllocate(totalSize);
  }

  @Test
  public void testSilentTryAllocate() throws Exception {
    final int totalSize = 10 * 1024 * 1024;

    final TlsfByteBufferManager spaceManager = new TlsfByteBufferManager(4, totalSize, true);

    final ByteBuffer buffer = spaceManager.allocate(totalSize);

    new Thread() {
      public void run() {
        try {
          logger.debug("Wait 2 sec for releasing buffer ...");
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          logger.error("caught exception", e);
        }

        spaceManager.release(buffer);
      }
    }.start();

    spaceManager.blockingTryAllocate(totalSize);
  }

  @Test
  public void testAllocateConcurrently() throws Exception {
    final int totalSize = 10 * 1024 * 1024;
    final TlsfByteBufferManager spaceManager = new TlsfByteBufferManager(4, totalSize, true);

    final int nAllocatingThreads = 100;
    final AtomicBoolean failed = new AtomicBoolean(false);
    final CountDownLatch threadLatch = new CountDownLatch(nAllocatingThreads);
    final CountDownLatch mainLatch = new CountDownLatch(1);
    final Random desiredSizeGenerator = new Random(System.currentTimeMillis());

    for (int i = 0; i < nAllocatingThreads; i++) {
      new Thread("Allocation " + i) {
        Random allocateOrRelease = new Random();
        List<ByteBuffer> bufferList = new ArrayList<>();

        public void run() {
          try {
            logger.debug("Thread {} started!", super.getName());
            mainLatch.await();
          } catch (InterruptedException e) {
            logger.error("caught exception", e);
          }

          try {
            for (int i = 0; i < 100; i++) {
              if (allocateOrRelease.nextBoolean()) {
                int desiredSize = (desiredSizeGenerator.nextBoolean()) ? 512 : 1024;

                try {
                  bufferList.add(spaceManager.allocate(desiredSize));
                } catch (OutOfSpaceException e) {
                  logger.debug("Caught an out of space exception!");
                  continue;
                }
              } else {
                if (bufferList.isEmpty()) {
                  continue;
                }

                Collections.shuffle(bufferList);
                ByteBuffer address = bufferList.remove(bufferList.size() - 1);
                spaceManager.release(address);
              }
            }

            for (ByteBuffer buffer : bufferList) {
              spaceManager.release(buffer);
            }

          } catch (Exception e) {
            logger.error("Caught an exception", e);
            failed.set(true);
          } finally {
            threadLatch.countDown();
          }
        }
      }.start();
    }

    mainLatch.countDown();
    threadLatch.await();

    Assert.assertFalse(failed.get());

    ByteBuffer buffer = spaceManager.allocate(totalSize);
    spaceManager.release(buffer);
  }

  @Test
  public void testAllocateAndReleaseInDifferentThread() throws Exception {
    final int totalSize = 100 * 1024 * 1024;
    final TlsfByteBufferManager spaceManager = new TlsfByteBufferManager(512, totalSize, true);
    final BlockingQueue<ByteBuffer> bufferQueue = new LinkedBlockingQueue<>();
    final ByteBuffer interruption = Mockito.mock(ByteBuffer.class);
    final int allocationSie = 129 * 1024;
    final int times = (totalSize / allocationSie) * 10;

    Thread releaseThread = new Thread(new Runnable() {
      @Override
      public void run() {
        ByteBuffer buffer = null;

        for (; ; ) {
          try {
            buffer = bufferQueue.take();
          } catch (InterruptedException e) {
            logger.warn("Caught an exception", e);
          }

          if (buffer == interruption) {
            break;
          }

          spaceManager.release(buffer);
        }
      }
    });

    releaseThread.start();

    ByteBuffer buffer;
    for (int i = 0; i < times; i++) {
      buffer = spaceManager.blockingAllocate(allocationSie);
      Assert.assertEquals(allocationSie, buffer.capacity());
      bufferQueue.offer(buffer);
    }

    bufferQueue.offer(interruption);
    releaseThread.join();
  }
}
