/**
* Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/ 

package py.test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.junit.Test;
import py.common.FastBuffer;
import py.common.FastBufferManager;
import py.common.FastBufferManagerLinearImpl;
import py.exception.NoAvailableBufferException;
import sun.misc.Unsafe;

public class TestFastbufferPerfomance extends TestBase {
  private final int count = 10000;
  ByteBuffer mmsrcByteBuffer1 = null;
  ByteBuffer mmsrcByteBuffer2 = null;
  private FastBufferManager fbManager = new FastBufferManagerLinearImpl(1024 * 1024 * 128);

  public TestFastbufferPerfomance() throws Exception {
    super.init();
  }

  @Test
  public void test() throws NoAvailableBufferException {
    Unsafe unsafe;
    try {
      Field field = Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      unsafe = (Unsafe) field.get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    long addressOfAllocatedMemory1 = unsafe.allocateMemory(1024 * 128);
    long addressOfAllocatedMemory2 = unsafe.allocateMemory(1024 * 128);

    long time1 = System.currentTimeMillis();
    for (int i = 0; i < count; i++) {
      unsafe
          .copyMemory(null, addressOfAllocatedMemory1, null, addressOfAllocatedMemory2, 1024 * 128);
    }

    long time2 = System.currentTimeMillis();
    logger.info("{} {} {}", time2 - time1, "unsafe", "unsafe");

    ByteBuffer mmppsrcByteBuffer = null;
    ByteBuffer mmppdstByteBuffer = null;
    FastBuffer fastBuf = fbManager.allocateBuffer(1024 * 128);
    byte[] srcba = new byte[1024 * 128];
    byte[] dstba = new byte[1024 * 128];
    ByteBuffer srcByteBuffer = ByteBuffer.allocate(1024 * 128);
    ByteBuffer dstByteBuffer = ByteBuffer.allocate(1024 * 128);

    time1 = System.currentTimeMillis();
    for (int i = 0; i < count; i++) {
      fastBuf.put(srcba);
    }

    time2 = System.currentTimeMillis();
    logger.info("{} {} {}", time2 - time1, "unsafe", "bytearray");

    time1 = System.currentTimeMillis();
    for (int i = 0; i < count; i++) {
      srcByteBuffer.clear();
      fastBuf.put(srcByteBuffer);
    }

    time2 = System.currentTimeMillis();
    logger.info("{} {} {}", time2 - time1, "unsafe", "bytebuffer");

    time1 = System.currentTimeMillis();
    for (int i = 0; i < count; i++) {
      System.arraycopy(dstba, 0, srcba, 0, 1024 * 128);
    }

    time2 = System.currentTimeMillis();
    logger.info("{} {} {}", time2 - time1, "bytearray", "bytearray");

    time1 = System.currentTimeMillis();
    for (int i = 0; i < count; i++) {
      srcByteBuffer.clear();
      dstByteBuffer.clear();
      srcByteBuffer.put(dstByteBuffer);
    }
    time2 = System.currentTimeMillis();
    logger.info("{} {} {}", time2 - time1, "bytebuffer", "bytebuffer");

    try {
      String filename1 = "src/test/resources/ramdisk/mmap";
      mmppsrcByteBuffer = createmmpp(filename1, 0);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      String filename2 = "src/test/resources/ramdisk/mmap2";

      mmppdstByteBuffer = createmmpp(filename2, 0);
    } catch (IOException e) {
      e.printStackTrace();
    }

    time1 = System.currentTimeMillis();
    for (int i = 0; i < count; i++) {
      mmppsrcByteBuffer.clear();
      mmppdstByteBuffer.clear();
      mmppsrcByteBuffer.put(mmppdstByteBuffer);
    }
    time2 = System.currentTimeMillis();
    logger.info("{} {} {}", time2 - time1, "mmap", "mmap");

    time1 = System.currentTimeMillis();
    for (int i = 0; i < count; i++) {
      mmppsrcByteBuffer.clear();
      fastBuf.put(mmppsrcByteBuffer);
    }
    time2 = System.currentTimeMillis();
    logger.info("{} {} {}", time2 - time1, "unsafe", "mmap");

    time1 = System.currentTimeMillis();
    for (int i = 0; i < count; i++) {
      srcByteBuffer.clear();
      mmppsrcByteBuffer.clear();
      srcByteBuffer.put(mmppsrcByteBuffer);
    }
    time2 = System.currentTimeMillis();

    logger.info("{} {} {}", time2 - time1, "bytebuffer", "mmap");

  }

  private ByteBuffer createmmpp(String filename, long offset) throws IOException {
    FileChannel channel = new RandomAccessFile(filename, "rw").getChannel();
    ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, offset, 128 * 1024);
    return buffer;
  }

}
