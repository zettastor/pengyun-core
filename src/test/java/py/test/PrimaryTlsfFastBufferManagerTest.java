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

import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import py.common.FastBuffer;
import py.common.FastBufferManager;
import py.common.PrimaryTlsfFastBufferManager;
import py.common.TlsfFastBufferManager;
import py.exception.NoAvailableBufferException;

public class PrimaryTlsfFastBufferManagerTest extends TestBase {
  public PrimaryTlsfFastBufferManagerTest() throws Exception {
    super.init();
  }

  @Test
  public void testAlignment() throws Exception {
    int alignment = 128;
    FastBufferManager fastBufferManager = new PrimaryTlsfFastBufferManager(alignment, 1024);
    List<FastBuffer> fastBuffers = new ArrayList<FastBuffer>();

    for (int i = 0; i < 100; i++) {
      try {
        fastBuffers.add(fastBufferManager.allocateBuffer(alignment));
      } catch (NoAvailableBufferException e) {
        logger.info("can not allocate buffer, new allocate buffers: {}, {}", fastBuffers.size(),
            fastBuffers);
        break;
      }
    }

    int releaseCount = fastBuffers.size();
    for (int i = 0; i < releaseCount; i++) {
      fastBufferManager.releaseBuffer(fastBuffers.get(i));
    }

    for (int i = 0; i < releaseCount; i++) {
      fastBuffers.add(fastBufferManager.allocateBuffer(1));
    }

    try {
      fastBufferManager.allocateBuffer(1);
      assertTrue(false);
    } catch (NoAvailableBufferException e) {
      logger.info("can not allocate buffer");
    }

    fastBufferManager.releaseBuffer(fastBuffers.get(0));
    try {
      fastBufferManager.allocateBuffer(alignment);
    } catch (NoAvailableBufferException e) {
      logger.error("can not allocate buffer");
      assertTrue(false);
    }
  }

  @Test
  public void testMaxAllocation() throws Exception {
    int alignment = 128;
    FastBufferManager fastBufferManager = new PrimaryTlsfFastBufferManager(alignment, 1024);
    List<FastBuffer> fastBuffers = new ArrayList<FastBuffer>();

    for (int i = 0; i < 100; i++) {
      try {
        fastBuffers.add(fastBufferManager.allocateBuffer(alignment));
      } catch (NoAvailableBufferException e) {
        logger.info("can not allocate buffer, new allocate buffers: {}, {}", fastBuffers.size(),
            fastBuffers);
        break;
      }
    }

    int releaseCount = fastBuffers.size();
    for (int i = 0; i < releaseCount; i++) {
      fastBufferManager.releaseBuffer(fastBuffers.get(i));
    }

    try {
      fastBufferManager.allocateBuffer(alignment * releaseCount);
    } catch (NoAvailableBufferException e) {
      logger.info("you can not allocate the buffer because the problem of fragment");
      assertTrue(false);
    }

    fastBuffers.clear();
    for (int i = 0; i < releaseCount; i++) {
      try {
        fastBuffers.add(fastBufferManager.allocateBuffer(alignment));
      } catch (NoAvailableBufferException e) {
        logger.info("can not allocate buffer, new allocate buffers: {}, {}", fastBuffers.size(),
            fastBuffers);
      }
    }

    releaseCount = 0;
    for (int i = 0; i < fastBuffers.size(); i += 2) {
      fastBufferManager.releaseBuffer(fastBuffers.get(i));
      releaseCount++;
    }

    try {
      fastBufferManager.allocateBuffer(alignment * (releaseCount + 1));
      assertTrue(false);
    } catch (NoAvailableBufferException e) {
      logger.info("you can not allocate the buffer because the problem of fragment");
    }

    try {
      fastBufferManager.allocateBuffer(alignment * releaseCount);
    } catch (NoAvailableBufferException e) {
      logger.info("you can not allocate the buffer because the problem of fragment");
      assertTrue(false);
    }
  }

  @Test
  public void testFragmentProblem() throws Exception {
   
    int alignment = 128;
    FastBufferManager fastBufferManager = new TlsfFastBufferManager(alignment, 1024);
    List<FastBuffer> fastBuffers = new ArrayList<FastBuffer>();
    for (int i = 0; i < 100; i++) {
      try {
        fastBuffers.add(fastBufferManager.allocateBuffer(alignment));
      } catch (NoAvailableBufferException e) {
        logger.info("can not allocate buffer, new allocate buffers: {}, {}", fastBuffers.size(),
            fastBuffers);
        break;
      }
    }

    for (int i = 0; i < fastBuffers.size(); i += 2) {
      fastBufferManager.releaseBuffer(fastBuffers.get(i));
    }

    try {
      fastBufferManager.allocateBuffer(alignment * 2);
      assertTrue(false);
    } catch (NoAvailableBufferException e) {
      logger.info("you can not allocate the buffer because the problem of fragment");
    }

    fastBuffers.clear();

    fastBufferManager = new PrimaryTlsfFastBufferManager(alignment, 1024);

    for (int i = 0; i < 100; i++) {
      try {
        fastBuffers.add(fastBufferManager.allocateBuffer(alignment));
      } catch (NoAvailableBufferException e) {
        logger.info("can not allocate buffer, new allocate buffers: {}, {}", fastBuffers.size(),
            fastBuffers);
        break;
      }
    }

    for (int i = 0; i < fastBuffers.size(); i += 2) {
      fastBufferManager.releaseBuffer(fastBuffers.get(i));
    }

    try {
      fastBufferManager.allocateBuffer(256);
    } catch (NoAvailableBufferException e) {
      logger.error("you can not allocate the buffer because the problem of fragment");
      assertTrue(false);
    }
  }

  @Test
  public void testGetAndPut() throws Exception {
    int alignment = 128;
    List<FastBuffer> fastBuffers = new ArrayList<FastBuffer>();
    FastBufferManager fastBufferManager = new PrimaryTlsfFastBufferManager(alignment, 1024 * 10);

    for (int i = 0; i < 100; i++) {
      try {
        fastBuffers.add(fastBufferManager.allocateBuffer(alignment));
      } catch (NoAvailableBufferException e) {
        logger.info("can not allocate buffer, new allocate buffers: {}, {}", fastBuffers.size(),
            fastBuffers);
        break;
      }
    }

    int releaseCount = 0;
    for (int i = 0; i < fastBuffers.size(); i += 2) {
      fastBufferManager.releaseBuffer(fastBuffers.get(i));
      releaseCount++;
    }

    FastBuffer fastBuffer = null;
    try {
      fastBuffer = fastBufferManager.allocateBuffer(releaseCount * alignment);
    } catch (NoAvailableBufferException e) {
      logger.error("you can not allocate the buffer because the problem of fragment");
      assertTrue(false);
    }

    int size = alignment * 2;
    byte[] src = get(size, (byte) 5);
    fastBuffer.put(src);
    byte[] des = new byte[size];
    fastBuffer.get(des);
    check(des, (byte) 5);

    size = alignment * 2 + 2;
    int offset = alignment * 2 - 1;
    src = get(size, (byte) 6);
    fastBuffer.put(offset, src, 0, src.length);
    des = new byte[size];
    fastBuffer.get(offset, des, 0, des.length);
    check(des, (byte) 6);

    size = alignment * 2 + 2;
    offset = alignment * 2 - 1;
    int bufferOffset = 1;
    src = get(size, (byte) 7);
    fastBuffer.put(offset, src, bufferOffset, src.length - bufferOffset * 2);
    des = new byte[size];
    fastBuffer.get(offset, des, bufferOffset, des.length - bufferOffset * 2);
    check(des, bufferOffset, des.length - bufferOffset * 2, (byte) 7);

    ByteBuffer srcBuffer = getBuffer(size, (byte) 8);
    fastBuffer.put(srcBuffer);
    ByteBuffer desBuffer = ByteBuffer.allocate(size);
    fastBuffer.get(desBuffer);
    check(desBuffer, (byte) 8);

    size = alignment * 2 + 2;
    offset = alignment * 2 - 1;
    srcBuffer = getBuffer(size, (byte) 9);
    fastBuffer.put(offset, srcBuffer, 0, srcBuffer.remaining());
    desBuffer = ByteBuffer.allocate(size);
    fastBuffer.get(offset, desBuffer, 0, desBuffer.remaining());
    check(desBuffer, (byte) 9);

    size = alignment * 2 + 2;
    offset = alignment * 2 - 1;
    bufferOffset = 1;
    srcBuffer = getBuffer(size, (byte) 16);
    fastBuffer.put(offset, srcBuffer, bufferOffset, srcBuffer.remaining() - bufferOffset * 2);
    desBuffer = ByteBuffer.allocate(size);
    fastBuffer.get(offset, desBuffer, bufferOffset, desBuffer.remaining() - bufferOffset * 2);
    check(desBuffer, bufferOffset, desBuffer.remaining() - bufferOffset * 2, (byte) 16);
  }

  public byte[] get(int size, byte value) {
    byte[] test = new byte[size];
    put(test, value);
    return test;
  }

  public ByteBuffer getBuffer(int size, byte value) {
    byte[] test = new byte[size];
    put(test, value);
    return ByteBuffer.wrap(test);
  }

  private void put(byte[] src, byte value) {
    for (int i = 0; i < src.length; i++) {
      src[i] = value;
    }
  }

  private void check(ByteBuffer src, byte value) {
    for (int i = 0; i < src.remaining(); i++) {
      assertTrue(src.get(i) == value);
    }
  }

  private void check(ByteBuffer src, int offset, int length, byte value) {
    for (int i = offset; i < offset + length; i++) {
      assertTrue(src.get(i) == value);
    }
  }

  private void check(byte[] src, byte value) {
    for (int i = 0; i < src.length; i++) {
      assertTrue(src[i] == value);
    }
  }

  private void check(byte[] src, int offset, int length, byte value) {
    for (int i = offset; i < offset + length; i++) {
      assertTrue(src[i] == value);
    }
  }
}
