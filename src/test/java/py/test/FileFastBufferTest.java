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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.FileFastBufferImpl;

public class FileFastBufferTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(FileFastBufferTest.class);
  private String filePath = "/tmp/test_dir/secondDir/";

  public FileFastBufferTest() {
  }

  @Override
  @Before
  public void init() throws IOException {
    Path path = Paths.get(filePath);
    if (!Files.exists(path)) {
      Files.createDirectories(path);
    }
    deleteFilesInDir(filePath);
  }

  @After
  public void clear() throws IOException {
    deleteFilesInDir(filePath);
    Files.deleteIfExists(Paths.get(filePath));
    Files.deleteIfExists(Paths.get(filePath).getParent());
  }

  public void deleteFilesInDir(String filePath) {
    File dir = new File(filePath);
    if (dir.isDirectory()) {
      String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        File file = new File(dir, children[i]);
        file.delete();
      }
    }
  }

  @Test
  public void testPutAndGet() {
    int number = 100;
    byte[] testArray = new byte[number];
    for (int i = 0; i < number; i++) {
      testArray[i] = (byte) i;
    }
    FileFastBufferImpl fileStore = new FileFastBufferImpl(filePath, testArray, number);

    byte[] readBuf = new byte[number];
    fileStore.get(readBuf);

    for (int i = 0; i < number; i++) {
      assertEquals(testArray[i], readBuf[i]);
    }
  }

  @Test
  public void testPutAndGetWithPosition() {
    int number = 100;
    int offset = 10;
    int length = 50;
    byte[] testArray = new byte[number];
    for (int i = 0; i < number; i++) {
      testArray[i] = (byte) i;
    }

    byte[] putArray = new byte[length];

    System.arraycopy(testArray, offset, putArray, 0, length);
    for (int i = 0; i < length; i++) {
      assertEquals(putArray[i], testArray[i + offset]);
    }
    FileFastBufferImpl fileStore = new FileFastBufferImpl(filePath, testArray, number);
    fileStore.put(testArray, offset, length);

    byte[] readBuf = new byte[length];
    fileStore.get(readBuf);

    for (int i = 0; i < length; i++) {
      assertEquals(putArray[i], readBuf[i]);
    }
  }

  @Test
  public void testByteBufferWrapper() {
    int number = 100;
    int offset = 10;
    int length = 50;
    byte[] testArray = new byte[number];
    for (int i = 0; i < number; i++) {
      testArray[i] = (byte) i;
    }
    ByteBuffer byteBuffer = ByteBuffer.wrap(testArray, offset, length);

    byte[] arrayFromByteBuffer = byteBuffer.array();
    for (int i = 0; i < length; i++) {
      assertEquals(testArray[i], arrayFromByteBuffer[i]);
    }
  }

  @Test
  public void testPutByteBufferAndGet() {
    int number = 100;
    int offset = 10;
    int length = 50;
    byte[] testArray = new byte[number];
    for (int i = 0; i < number; i++) {
      testArray[i] = (byte) i;
    }
    byte[] putArray = new byte[length];

    System.arraycopy(testArray, offset, putArray, 0, length);

    ByteBuffer byteBuffer = ByteBuffer.wrap(testArray, offset, length);
    FileFastBufferImpl fileStore = new FileFastBufferImpl(filePath, testArray, number);

    fileStore.put(byteBuffer);

    byte[] readBuf = new byte[length];
    fileStore.get(readBuf);

    for (int i = 0; i < length; i++) {
      assertEquals(readBuf[i], putArray[i]);
    }
  }

  @Test
  public void testPutAndGetPart() {
    int number = 100;
    int offset = 10;
    int length = 50;
    byte[] testArray = new byte[number];
    for (int i = 0; i < number; i++) {
      testArray[i] = (byte) i;
    }
    byte[] putArray = new byte[length];

    System.arraycopy(testArray, offset, putArray, 0, length);
    FileFastBufferImpl fileStore = new FileFastBufferImpl(filePath, testArray, number);

    byte[] readBuf = new byte[length + offset];
    fileStore.get(offset, readBuf, offset, length);

    for (int i = 0; i < length; i++) {
      assertEquals(readBuf[i + offset], putArray[i]);
    }
  }

  @Test
  public void testToArray() {
    int number = 100;
    byte[] testArray = new byte[number];
    for (int i = 0; i < number; i++) {
      testArray[i] = (byte) i;
    }
    FileFastBufferImpl fileStore = new FileFastBufferImpl(filePath, testArray, number);

    byte[] array = fileStore.array();

    for (int i = 0; i < number; i++) {
      assertEquals(testArray[i], array[i]);
    }
  }

  @Test
  public void testGetByteBuffer() {
    int number = 100;
    byte[] testArray = new byte[number];
    for (int i = 0; i < number; i++) {
      testArray[i] = (byte) i;
    }
    FileFastBufferImpl fileStore = new FileFastBufferImpl(filePath, testArray, number);
    ByteBuffer buffer = ByteBuffer.allocate(number);
    fileStore.get(buffer);
    byte[] readBuf = buffer.array();
    for (int i = 0; i < number; i++) {
      assertEquals(testArray[i], readBuf[i]);
    }
  }
}
