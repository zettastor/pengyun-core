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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import py.common.FastBuffer;
import py.common.FastBufferManagerLinearImpl;
import py.exception.BufferOverflowException;
import py.exception.BufferUnderflowException;
import py.exception.NoAvailableBufferException;

@SuppressWarnings("deprecation")
public class TestFastBufferManagerLinearImpl extends TestBase {
  private static final long MAX_SIZE_OF_ALLOCATED_MEM = 1024L * 128L;
  static AtomicInteger numThreadExit = new AtomicInteger(0);
  byte[] byteInitialBuffer = new byte[1024];
  private FastBufferManagerLinearImpl fbManager = new FastBufferManagerLinearImpl(
      MAX_SIZE_OF_ALLOCATED_MEM);

  @Override
  @Before
  public void init() throws Exception {
    super.init();

    for (int i = 0; i < 1024; i++) {
      byteInitialBuffer[i] = (byte) (i % 256);
    }
    fbManager.printBuffer("alloc buffer");
  }

  @Test
  public void testByteBuffer() throws Exception {
    ByteBuffer srcByteBuffer = ByteBuffer.allocate(10);
    for (int i = 0; i < 10; i++) {
      srcByteBuffer.put((byte) i);
    }
    srcByteBuffer.clear();
    FastBuffer fastBuf = fbManager.allocateBuffer(10);
    fastBuf.put(srcByteBuffer);

    assertArrayEquals(srcByteBuffer.array(), fastBuf.array());
    ByteBuffer dstByteBuffer = ByteBuffer.allocate(10);
    fastBuf.get(dstByteBuffer);

    for (int i = 0; i < 10; i++) {
      assertEquals(srcByteBuffer.get(i), dstByteBuffer.get(i));
    }

    byte[] dst = new byte[10];
    fastBuf.get(dst);
    assertEquals((byte) 0, dst[0]);
    assertEquals((byte) 5, dst[5]);
    assertEquals((byte) 9, dst[9]);
  }

  @Test
  public void bulkPutAndThenBulkGet() throws Exception {
    FastBuffer buffer = fbManager.allocateBuffer(128 * 4);

    ByteBuffer srcBuffer = ByteBuffer.allocateDirect((int) (buffer.size()));
    for (int i = 0; i < srcBuffer.capacity(); i++) {
      srcBuffer.put((byte) (i % Byte.MAX_VALUE));
    }

    srcBuffer.clear();
    buffer.put(srcBuffer, 0, srcBuffer.remaining());

    ByteBuffer dstBuffer = ByteBuffer.allocateDirect((int) buffer.size());
    buffer.get(dstBuffer, 0, dstBuffer.remaining());

    srcBuffer.clear();
    dstBuffer.clear();
    for (int i = 0; i < dstBuffer.capacity(); i++) {
      Assert.assertTrue(srcBuffer.get(i) == dstBuffer.get(i));
    }
    fbManager.releaseBuffer(buffer);
  }

  @Test
  public void testByteBufferAbnomal() throws Exception {
    ByteBuffer srcByteBuffer = ByteBuffer.allocate(100);
    FastBuffer fastBuf = fbManager.allocateBuffer(10);
    try {
      fastBuf.put(srcByteBuffer);
    } catch (BufferOverflowException e) {
      logger.error("caught exception", e);
    } catch (Exception e) {
      fail();
    }

    ByteBuffer dstByteBuffer = ByteBuffer.allocate(100);

    try {
      fastBuf.get(dstByteBuffer);
    } catch (BufferUnderflowException e) {
      logger.error("caught exception", e);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void testBase() throws Exception {
    FastBuffer buf1 = null;

    try {
      buf1 = fbManager.allocateBuffer(1024);
    } catch (NoAvailableBufferException e) {
      fail();
    }

    byte[] a = new byte[1024];

    buf1.put(byteInitialBuffer);

    buf1.get(a);

    int i;

    for (i = 0; i < 1024; i++) {
      assertEquals(a[i], byteInitialBuffer[i]);
    }

    fbManager.releaseBuffer(buf1);

    List<UseMemoryTask> tasks = new ArrayList<>();
    for (i = 0; i < 100; i++) {
      UseMemoryTask task = new UseMemoryTask();
      Thread t = new Thread(task);
      tasks.add(task);
      t.start();
    }

    while (numThreadExit.get() < 100) {
      try {
        Thread.sleep(2000);
        logger.info("still working");
      } catch (InterruptedException e) {
        fail("interrupted");
      }
      if (numThreadExit.get() > 0) {
        logger.info("-----numThreadExit is  [" + numThreadExit.get() + "] ----- ");
      }
    }

    long addressInitial = fbManager.getEntryAddress(1);
    assertEquals(addressInitial, fbManager.getEntryAddress(1));
    long sizeInitial = fbManager.getEntrySize(1);
    assertEquals(sizeInitial, fbManager.getEntrySize(1));
    fbManager.printBuffer("release over");

    for (i = 0; i < 100; i++) {
      assertFalse(tasks.get(i).failed);
    }
  }

  @Test
  public void testAbnormal() {
    try {
      FastBuffer buf1 = fbManager.allocateBuffer(2);
      byte[] dst = new byte[10];

      try {
        buf1.get(dst);
        fail();
      } catch (BufferUnderflowException e) {
        logger.error("caught exception", e);
      } catch (Exception e) {
        fail();
      }

      byte[] src = new byte[3];

      try {
        buf1.put(src);
        fail();
      } catch (BufferOverflowException e) {
        logger.error("caught exception", e);
      } catch (Exception e) {
        fail();
      }
    } catch (NoAvailableBufferException e) {
      fail();
    }
  }

  private class UseMemoryTask implements Runnable {
    ArrayList<FastBuffer> bufList = new ArrayList<FastBuffer>();
    Random randForSleep = new Random(System.currentTimeMillis());
    Random randForAllocation = new Random(System.currentTimeMillis() + 100);
    Random randForWhichBufferToFree = new Random(System.currentTimeMillis() + 500);
    Random randForFreeBufferOrAllocateBuf = new Random(System.currentTimeMillis() + 10000);
    boolean failed = false;

    @Override
    public void run() {
      int times = 0;
      while (times < 1000) {
        if (randForSleep.nextBoolean()) {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            logger.error("caught exception", e);
            failed = true;
            break;
          }
        }

        if (randForFreeBufferOrAllocateBuf.nextBoolean()) {
          int size = bufList.size();

          if (size > 0) {
            int index = randForWhichBufferToFree.nextInt(size);
            try {
              fbManager.releaseBuffer(bufList.get(index));
            } catch (Exception e) {
              logger.error("caught exception", e);
              failed = true;
              break;
            }
            bufList.remove(index);
          }
        } else {
          times++;

          int sizeToAllocate = randForAllocation.nextInt((int) MAX_SIZE_OF_ALLOCATED_MEM);
          if (sizeToAllocate == 0) {
            sizeToAllocate = 10;
          }

          FastBuffer buf = null;
          try {
            buf = fbManager.allocateBuffer(sizeToAllocate);
            buf.put(byteInitialBuffer, 0, sizeToAllocate);
            bufList.add(buf);
          } catch (NoAvailableBufferException e) {
            logger.error("caught exception", e);
          } catch (Exception e) {
            logger.error("caught exception", e);
            failed = true;
            break;
          }
        }
      }

      int nums = bufList.size();
      for (int i = 0; i < nums; i++) {
        try {
          fbManager.releaseBuffer(bufList.get(i));
        } catch (Exception e) {
          logger.error("caught exception", e);
          failed = true;
          break;
        }
      }
      addOne();
    }

    void addOne() {
      numThreadExit.incrementAndGet();
    }
  }
}
