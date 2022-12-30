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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.FastBuffer;
import py.common.TlsfFastBufferManager;
import py.common.Utils;
import py.exception.NoAvailableBufferException;

public class TlsfFastBufferManagerTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(TlsfFastBufferManagerTest.class);

  private static final AtomicInteger numThreadExit = new AtomicInteger(0);

  private static final int METADATA_UNIT_BYTES = Long.SIZE / Byte.SIZE;

  private static final int METADATA_SIZE_BYTES = 5 * METADATA_UNIT_BYTES;

  private static final int MIN_ACCESSIBLE_MEM_SIZE = 3 * METADATA_UNIT_BYTES;

  private static final int MIN_BUFFER_SIZE = 3 * METADATA_UNIT_BYTES + MIN_ACCESSIBLE_MEM_SIZE;

  private final long pageSize = 4;

  private FastBufferManagerDirectImplForTest bufferManager;

  @Override
  @Before
  public void init() throws Exception {
    super.init();
    numThreadExit.set(0);
    bufferManager = null;
  }

  @After
  public void close() throws Exception {
    if (bufferManager != null) {
      bufferManager.close();
    }
  }

  @Test
  public void allocateOneByteMoreSize() throws Exception {
    bufferManager = new FastBufferManagerDirectImplForTest(
        new TlsfFastBufferManager(MIN_BUFFER_SIZE));

    try {
      bufferManager.allocateBuffer(MIN_ACCESSIBLE_MEM_SIZE + 1);
      fail();
    } catch (NoAvailableBufferException e) {
      logger.error("caught exception", e);
    }
  }

  @Test
  public void allocateJustMemorySize() throws Exception {
    bufferManager = new FastBufferManagerDirectImplForTest(
        new TlsfFastBufferManager(MIN_BUFFER_SIZE));

    FastBuffer buffer = bufferManager.allocateBuffer(MIN_ACCESSIBLE_MEM_SIZE);
    try {
      bufferManager.allocateBuffer(1);
      fail();
    } catch (NoAvailableBufferException e) {
      logger.error("caught exception", e);
    }

    bufferManager.releaseBuffer(buffer);
  }

  @Test
  public void allocateAfterRelease() throws Exception {
    bufferManager = new FastBufferManagerDirectImplForTest(
        new TlsfFastBufferManager(MIN_BUFFER_SIZE));

    FastBuffer buffer = bufferManager.allocateBuffer(MIN_ACCESSIBLE_MEM_SIZE);

    Assert.assertEquals(MIN_ACCESSIBLE_MEM_SIZE, buffer.size());

    bufferManager.releaseBuffer(buffer);

    buffer = bufferManager.allocateBuffer(MIN_ACCESSIBLE_MEM_SIZE);

    bufferManager.releaseBuffer(buffer);
  }

  @Test
  public void allocateZeroOrEvenSmaller() throws Exception {
    TlsfFastBufferManager bufferManager = new TlsfFastBufferManager(MIN_BUFFER_SIZE);
    Assert.assertNull(bufferManager.allocateBuffer(0));
    Assert.assertNull(bufferManager.allocateBuffer(-1));
  }

  @Test
  public void allocateJustThreeMinAccessibleMem() throws Exception {
    long totalSize = 3 * METADATA_SIZE_BYTES + METADATA_UNIT_BYTES;

    for (; totalSize < 4 * METADATA_SIZE_BYTES + METADATA_UNIT_BYTES; totalSize += 4) {
      TlsfFastBufferManager fastBufferManager = new TlsfFastBufferManager(totalSize);
      bufferManager = new FastBufferManagerDirectImplForTest(fastBufferManager);

      List<FastBuffer> bufferList = new ArrayList<FastBuffer>();
      while (bufferList.size() < 3) {
        FastBuffer buffer = bufferManager.allocateBuffer(MIN_ACCESSIBLE_MEM_SIZE);
        buffer.put(new byte[(int) buffer.size()]);
        bufferList.add(buffer);
      }

      try {
        bufferManager.allocateBuffer(1);
        fail();
      } catch (Exception e) {
        logger.error("caught exception", e);
      }

      for (FastBuffer buffer : bufferList) {
        bufferManager.releaseBuffer(buffer);
      }

      UseMemoryTask task = new UseMemoryTask(totalSize, MIN_ACCESSIBLE_MEM_SIZE);
      task.totalTimes = 20;
      task.run();

      Assert.assertFalse(task.failed);
      Assert.assertEquals(totalSize - 3 * METADATA_UNIT_BYTES,
          fastBufferManager.getFirstAccessibleMemSize());

      bufferManager.close();
    }
  }

  @Test
  public void allocateBufferForMostTimes() throws Exception {
    long totalSize = 1024 * 1024;
    List<FastBuffer> bufferList = new ArrayList<FastBuffer>();
    TlsfFastBufferManager fastBufferManager = new TlsfFastBufferManager(totalSize);

    List<Integer> requestSizeList = new ArrayList<Integer>();
    requestSizeList.add(MIN_ACCESSIBLE_MEM_SIZE);
    requestSizeList.add(Long.SIZE * 4);

    for (int requestSize : requestSizeList) {
      while (true) {
        try {
          FastBuffer buffer = fastBufferManager.allocateBuffer(requestSize);
          buffer.put(new byte[(int) buffer.size()]);
          bufferList.add(buffer);
        } catch (NoAvailableBufferException e) {
          break;
        }
      }

      Assert
          .assertEquals((totalSize - METADATA_UNIT_BYTES) / (requestSize + 2 * METADATA_UNIT_BYTES),
              bufferList.size());

      for (FastBuffer buffer : bufferList) {
        fastBufferManager.releaseBuffer(buffer);
      }

      Assert.assertEquals(totalSize - 3 * METADATA_UNIT_BYTES,
          fastBufferManager.getFirstAccessibleMemSize());
      bufferList.clear();
    }
    fastBufferManager.close();
  }

  @Test
  public void allocateBufferForMostTimesAgain() throws Exception {
    long totalSize = 1024 * 1024;
    List<FastBuffer> bufferList = new ArrayList<FastBuffer>();

    TlsfFastBufferManager bufferManager = new TlsfFastBufferManager(totalSize);

    boolean done = false;
    while (!done) {
      try {
        bufferList.add(bufferManager.allocateBuffer(1024));
      } catch (NoAvailableBufferException e) {
        done = true;
      }
    }

    for (int i = 0; i < bufferList.size(); i++) {
      bufferManager.releaseBuffer(bufferList.get(i));
    }

    for (int i = 0; i < bufferList.size(); i++) {
      bufferManager.allocateBuffer(1024);
    }

    logger.debug("Allocated {} buffers", bufferList.size());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMappingFunction() throws Exception {
    TlsfFastBufferManager fastBufferManager = new TlsfFastBufferManager(MIN_BUFFER_SIZE);
    Method method = fastBufferManager.getClass().getDeclaredMethod("mapping", long.class);
    method.setAccessible(true);

    Pair<Integer, Integer> fliWithSli = (Pair<Integer, Integer>) method
        .invoke(fastBufferManager, 88);
    Assert.assertEquals(0, fliWithSli.getLeft().intValue());
    Assert.assertEquals(22, fliWithSli.getRight().intValue());

    fliWithSli = (Pair<Integer, Integer>) method.invoke(fastBufferManager, 240);
    Assert.assertEquals(0, fliWithSli.getLeft().intValue());
    Assert.assertEquals(60, fliWithSli.getRight().intValue());

    fliWithSli = (Pair<Integer, Integer>) method.invoke(fastBufferManager, 460);
    Assert.assertEquals(1, fliWithSli.getLeft().intValue());
    Assert.assertEquals(51, fliWithSli.getRight().intValue());

    fliWithSli = (Pair<Integer, Integer>) method.invoke(fastBufferManager, 888);
    Assert.assertEquals(2, fliWithSli.getLeft().intValue());
    Assert.assertEquals(47, fliWithSli.getRight().intValue());
  }

  @Test
  public void allocateNotAlignedSizeForMostTimes() throws Exception {
    long totalSize = 1024 * 1024;
    TlsfFastBufferManager fastBufferManager = new TlsfFastBufferManager(totalSize);

    Random randForAllocation = new Random(System.currentTimeMillis() + 100);
    List<FastBuffer> bufferList = new ArrayList<FastBuffer>();

    long totalAllocatedMem = 0L;
    for (int i = 0; i < 1000; i++) {
      int sizeToAllocate = randForAllocation
          .nextInt((int) (totalSize - totalAllocatedMem - 3 * METADATA_UNIT_BYTES));
      sizeToAllocate = (sizeToAllocate == 0) ? 1 : sizeToAllocate;
      try {
        FastBuffer buffer = fastBufferManager.allocateBuffer(sizeToAllocate);
        totalAllocatedMem += buffer.size();
        bufferList.add(buffer);
      } catch (NoAvailableBufferException e) {
        logger.error("caught exception", e);
      }
    }

    for (FastBuffer buffer : bufferList) {
      fastBufferManager.releaseBuffer(buffer);
    }

    Assert.assertEquals(totalSize - 3 * METADATA_UNIT_BYTES,
        fastBufferManager.getFirstAccessibleMemSize());
  }

  @Test
  public void interphaseAllocationAndReleasing() throws Exception {
    long totalSize = 100 * 1024 * 1024;
    TlsfFastBufferManager fastBufferManager = new TlsfFastBufferManager(totalSize);
    bufferManager = new FastBufferManagerDirectImplForTest(fastBufferManager);

    List<UseMemoryTask> tasks = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      UseMemoryTask task = new UseMemoryTask(totalSize);
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

    logger.info("all threads are done, Verify whether all of them are good");
    for (int i = 0; i < 100; i++) {
      assertFalse(tasks.get(i).failed);
    }

    Assert.assertEquals(totalSize - 3 * METADATA_UNIT_BYTES,
        fastBufferManager.getFirstAccessibleMemSize());
  }

  @Test
  public void controlSpeedOfAllocationAndRelease() throws Exception {
    final Queue<FastBuffer> bufferQueue = new LinkedBlockingQueue<FastBuffer>();
    long totalSize = 100 * 1024 * 1024;
    final TlsfFastBufferManager fastBufferManager = new TlsfFastBufferManager(totalSize);

    try {
      while (true) {
        bufferQueue.offer(fastBufferManager.allocateBuffer(1024));
      }
    } catch (NoAvailableBufferException e) {
      logger.error("caught exception", e);
    }

    final Queue<FastBuffer> bufferQueue1 = new LinkedBlockingQueue<>();
    Thread allocationThread = new Thread() {
      @Override
      public void run() {
        while (!Thread.interrupted()) {
          try {
            bufferQueue1.offer(fastBufferManager.allocateBuffer(1024));
          } catch (NoAvailableBufferException e) {
            logger.error("caught exception", e);
          }
        }
      }
    };

    allocationThread.start();

    Assert.assertTrue(100 < bufferQueue.size());
    for (int i = 0; i < 100; i++) {
      fastBufferManager.releaseBuffer(bufferQueue.poll());
      Thread.sleep(10);
    }

    allocationThread.interrupt();
    logger.debug("Allocation speed: {} buffers per second", bufferQueue1.size());
    Assert.assertTrue(90 < bufferQueue1.size() && bufferQueue1.size() <= 100);

    for (FastBuffer buffer : bufferQueue) {
      fastBufferManager.releaseBuffer(buffer);
    }

    for (FastBuffer buffer : bufferQueue1) {
      fastBufferManager.releaseBuffer(buffer);
    }
  }

  @Test
  public void putAndThenGet() throws Exception {
    long totalSize = 1024 * 1024;
    TlsfFastBufferManager fastBufferManager = new TlsfFastBufferManager(totalSize);

    FastBuffer buffer = fastBufferManager.allocateBuffer(128 * pageSize);

    byte[] src = new byte[(int) (128 * pageSize)];
    for (int i = 0; i < 128 * pageSize; i++) {
      src[i] = (byte) (i % Byte.MAX_VALUE);
    }

    buffer.put(src);

    byte[] dst = new byte[src.length];
    buffer.get(dst);

    for (int i = 0; i < dst.length; i++) {
      Assert.assertTrue(dst[i] == src[i]);
    }

    fastBufferManager.releaseBuffer(buffer);
  }

  @Test
  public void putOffsetAndThenGetOffset() throws Exception {
    long totalSize = 1024 * 1024;
    TlsfFastBufferManager fastBufferManager = new TlsfFastBufferManager(totalSize);

    FastBuffer buffer = fastBufferManager.allocateBuffer(128 * pageSize);

    int length = (int) (128 * pageSize);
    int offset = new Random(System.currentTimeMillis()).nextInt(length - 1);

    byte[] src = new byte[offset + length];
    for (int i = offset; i < src.length; i++) {
      src[i] = (byte) (i % Byte.MAX_VALUE);
    }

    buffer.put(src, offset, length);

    byte[] dst = new byte[src.length];
    buffer.get(dst, offset, length);

    for (int i = offset; i < dst.length; i++) {
      Assert.assertTrue(dst[i] == src[i]);
    }

    fastBufferManager.releaseBuffer(buffer);
  }

  @Test
  public void putBufferAndThenGetBuffer() throws Exception {
    long totalSize = 1024 * 1024;
    TlsfFastBufferManager fastBufferManager = new TlsfFastBufferManager(totalSize);

    FastBuffer buffer = fastBufferManager.allocateBuffer(128 * pageSize);
    logger.debug("Allocated {} pages", buffer.size() / pageSize);

    ByteBuffer srcBuffer = ByteBuffer.allocate((int) (buffer.size()));
    for (int i = 0; i < srcBuffer.capacity(); i++) {
      srcBuffer.put((byte) (i % Byte.MAX_VALUE));
    }

    srcBuffer.clear();
    buffer.put(srcBuffer);

    ByteBuffer dstBuffer = ByteBuffer.allocate(srcBuffer.capacity());
    buffer.get(dstBuffer);
    srcBuffer.clear();
    dstBuffer.clear();
    for (int i = 0; i < dstBuffer.capacity(); i++) {
      Assert.assertTrue(srcBuffer.get(i) == dstBuffer.get(i));
    }
    fastBufferManager.releaseBuffer(buffer);
  }

  @Test
  public void bulkPutAndThenBulkGet() throws Exception {
    long totalSize = 1024 * 1024;
    TlsfFastBufferManager fastBufferManager = new TlsfFastBufferManager(totalSize);

    FastBuffer buffer = fastBufferManager.allocateBuffer(128 * pageSize);
    logger.debug("Allocated {} pages", buffer.size() / pageSize);

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
    fastBufferManager.releaseBuffer(buffer);
  }

  @Test
  public void comparePerformanceWithHeapByteBuffer() throws Exception {
    Random randForAllocationSize = new Random(System.currentTimeMillis());

    List<Integer> requestSizeList = new ArrayList<Integer>();
    for (int i = 0; i < 5000; i++) {
      int requestSize = randForAllocationSize.nextInt(10 * 1024);
      requestSizeList.add((requestSize == 0) ? 1 : requestSize);
    }

    TlsfFastBufferManager fastBufferManager = new TlsfFastBufferManager(100 * 1024 * 1024);

    List<ByteBuffer> byteBufferList = new ArrayList<ByteBuffer>();

    long curTime = System.currentTimeMillis();
    for (int i = 0; i < 5000; i++) {
      byteBufferList.add(ByteBuffer.allocate(requestSizeList.get(i)));
    }
    long elapsedTime = System.currentTimeMillis() - curTime;
    System.out
        .format("Elapsed time for byte buffer allocation with empty heap: %s ms\n", elapsedTime);

    byteBufferList.clear();

    curTime = System.currentTimeMillis();
    for (int i = 0; i < 5000; i++) {
      byteBufferList.add(ByteBuffer.allocate(requestSizeList.get(i)));
    }
    elapsedTime = System.currentTimeMillis() - curTime;
    System.out
        .format("Elapsed time for byte buffer allocation with used heap: %s ms\n", elapsedTime);

    byteBufferList.clear();

    List<FastBuffer> fastBufferList = new ArrayList<FastBuffer>();
    curTime = System.currentTimeMillis();
    for (int i = 0; i < 5000; i++) {
      fastBufferList.add(fastBufferManager.allocateBuffer(requestSizeList.get(i)));
    }
    elapsedTime = System.currentTimeMillis() - curTime;
    System.out
        .format("Elapsed time for fast buffer allocation before release: %s ms\n", elapsedTime);

    for (FastBuffer buffer : fastBufferList) {
      fastBufferManager.releaseBuffer(buffer);
    }
    fastBufferList.clear();

    curTime = System.currentTimeMillis();
    for (int i = 0; i < 5000; i++) {
      fastBufferList.add(fastBufferManager.allocateBuffer(requestSizeList.get(i)));
    }
    elapsedTime = System.currentTimeMillis() - curTime;
    System.out
        .format("Elapsed time for fast buffer allocation after release: %s ms\n", elapsedTime);

    for (FastBuffer buffer : fastBufferList) {
      fastBufferManager.releaseBuffer(buffer);
    }
    fastBufferList.clear();
  }

  @Test
  public void releaseMultipleTimes() throws Exception {
    TlsfFastBufferManager fastBufferManager = new TlsfFastBufferManager(MIN_BUFFER_SIZE);
    FastBuffer buffer = fastBufferManager.allocateBuffer(MIN_ACCESSIBLE_MEM_SIZE);
    fastBufferManager.releaseBuffer(buffer);
    fastBufferManager.releaseBuffer(buffer);
    buffer = fastBufferManager.allocateBuffer(MIN_ACCESSIBLE_MEM_SIZE);
    try {
      fastBufferManager.allocateBuffer(1);
      fail();
    } catch (NoAvailableBufferException e) {
      logger.error("caught exception", e);
    }
    fastBufferManager.releaseBuffer(buffer);
  }

  @Test
  public void checkIfByteBufferFromFileChannel() throws Exception {
    File file = new File("/tmp/tlsffastbuffer");
    Utils.createOrExtendFile(file, 1024);

    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
    FileChannel fileChannel = randomAccessFile.getChannel();
    ByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 1024);

    Assert.assertTrue(isDirectBuffer(byteBuffer));
    Assert.assertTrue(byteBuffer.isDirect());
  }

  private boolean isDirectBuffer(ByteBuffer byteBuffer) throws Exception {
    PrintStream out = System.out;

    Class<?> c = byteBuffer.getClass();
    System.out.println(c.getCanonicalName());
    Buffer buffer = byteBuffer;

    out.format("Class:%n  %s%n%n", c.getCanonicalName());
    out.format("Modifiers:%n  %s%n%n", Modifier.toString(c.getModifiers()));

    out.format("Type Parameters:%n");
    TypeVariable[] tv = c.getTypeParameters();
    if (tv.length != 0) {
      out.format("  ");
      for (TypeVariable t : tv) {
        out.format("%s ", t.getName());
      }
      out.format("%n%n");
    } else {
      out.format("  -- No Type Parameters --%n%n");
    }

    out.format("Implemented Interfaces:%n");
    Type[] intfs = c.getGenericInterfaces();
    if (intfs.length != 0) {
      for (Type intf : intfs) {
        out.format("  %s%n", intf.toString());
      }
      out.format("%n");
    } else {
      out.format("  -- No Implemented Interfaces --%n%n");
    }

    out.format("Inheritance Path:%n");
    List<Class> l = new ArrayList<Class>();
    if (l.size() != 0) {
      for (Class<?> cl : l) {
        out.format("  %s%n", cl.getCanonicalName());
      }
      out.format("%n");
    } else {
      out.format("  -- No Super Classes --%n%n");
    }

    out.format("Annotations:%n");
    Annotation[] ann = c.getAnnotations();
    if (ann.length != 0) {
      for (Annotation a : ann) {
        out.format("  %s%n", a.toString());
      }
      out.format("%n");
    } else {
      out.format("  -- No Annotations --%n%n");
    }

    return true;
  }

  public long moveToLow(long value) {
    return 1 << (Long.SIZE - Long.numberOfLeadingZeros(value) - 1);
  }

  public class UseMemoryTask implements Runnable {
    private final long totalAccessableMemSize;
    private Random randForSleep = new Random(System.currentTimeMillis());
    private Random randForAllocation = new Random(System.currentTimeMillis() + 100);
    private Random randForWhichBufferToFree = new Random(System.currentTimeMillis() + 500);
    private Random randForFreeBufferOrAllocateBuf = new Random(System.currentTimeMillis() + 10000);
    private Random randForUsingTimeout = new Random(System.currentTimeMillis() + 15000);
    private boolean failed = false;
    private boolean randomSize = true;
    private ArrayList<FastBuffer> bufList = new ArrayList<FastBuffer>();
    private long allocationSize;
    private int totalTimes = 1000;

    public UseMemoryTask(long totalAccessableMemSize) {
      this.totalAccessableMemSize = totalAccessableMemSize;
    }

    public UseMemoryTask(long totalAccessableMemSize, long allocationSize) {
      this.totalAccessableMemSize = totalAccessableMemSize;
      this.allocationSize = allocationSize;
      randomSize = false;
    }

    @Override
    public void run() {
      int times = 0;

      while (times < totalTimes) {
        if (randForSleep.nextBoolean()) {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            logger.error("caught exception", e);
          }
        }

        if (randForFreeBufferOrAllocateBuf.nextBoolean()) {
          int size = bufList.size();

          if (size > 0) {
            int index = randForWhichBufferToFree.nextInt(size);
            try {
              bufferManager.releaseBuffer(bufList.get(index));
            } catch (Exception e) {
              logger.error("Caught an exception", e);
              failed = true;
              break;
            }
            bufList.remove(index);
          }
        } else {
          times++;

          int sizeToAllocate =
              (randomSize) ? randForAllocation.nextInt((int) totalAccessableMemSize)
                  : (int) allocationSize;
          if (sizeToAllocate == 0) {
            sizeToAllocate = MIN_ACCESSIBLE_MEM_SIZE;
          }

          FastBuffer buf = null;
          try {
            if (randForUsingTimeout.nextBoolean()) {
              buf = bufferManager.allocateBuffer(sizeToAllocate);
            } else {
              buf = bufferManager.allocateBuffer(sizeToAllocate, 200);
            }
            buf.put(new byte[(int) buf.size()]);
            bufList.add(buf);
          } catch (NoAvailableBufferException e) {
            logger.error("caught exception", e);
          } catch (Exception e) {
            logger.error("Caught an exception", e);
            failed = true;
            break;
          }
        }
      }

      int nums = bufList.size();
      for (int i = 0; i < nums; i++) {
        try {
          bufferManager.releaseBuffer(bufList.get(i));
        } catch (Exception e) {
          logger.error("Caught an exception", e);
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
