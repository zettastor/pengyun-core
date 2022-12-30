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

package py.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.exception.BufferOverflowException;
import py.exception.BufferUnderflowException;

public class FileFastBufferImpl implements FastBuffer {
  private static final Logger logger = LoggerFactory.getLogger(FileFastBufferImpl.class);
  private final int length;
  private File fileStoringData;

  /**
   * constructor.
   */
  public FileFastBufferImpl(String filePath, byte[] src, int length) {
    String fileName = String.valueOf(RequestIdBuilder.get());
    // at the beginning, set the bufferSize zero
    this.length = length;
    fileStoringData = new File(filePath, fileName);
    put(src);
  }

  @Override
  public void get(byte[] dst) throws BufferUnderflowException {
    get(dst, 0, dst.length);
  }

  @Override
  public void get(byte[] dst, int offset, int length) throws BufferUnderflowException {
    get(0, dst, offset, length);
  }

  @Override
  public void get(long myOffset, byte[] dst, int offset, int length)
      throws BufferUnderflowException {
    if (this.length < length) {
      throw new BufferUnderflowException("dst' length " + length + " is larger than the file size "
          + this.length);
    }
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(fileStoringData);
      // skip myOffset bytes, then try to read from file
      fis.skip(myOffset);
      fis.read(dst, offset, length);
    } catch (IOException e) {
      logger.error("can't get data from the channel at the position {} ", myOffset);
      throw new RuntimeException();
    } finally {
      try {
        fis.close();
      } catch (IOException e) {
        logger.error("failed to close file handler", e);
      }
    }
  }

  @Override
  public void get(ByteBuffer dst) throws BufferUnderflowException {
    if (dst.remaining() < this.length) {
      throw new BufferUnderflowException(
          "dst' length " + dst.remaining() + " is larger than the file size "
              + length);
    }

    int originLimit = dst.limit();
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(fileStoringData);
      FileChannel fileChannel = fis.getChannel();
      dst.limit(dst.position() + this.length);
      fileChannel.read(dst);
    } catch (Exception e) {
      logger.error("can't get data from the file");
      throw new RuntimeException();
    } finally {
      dst.limit(originLimit);
      try {
        fis.close();
      } catch (IOException e) {
        logger.error("failed to close file handler", e);
      }
    }
  }

  @Override
  public void get(ByteBuffer dst, int dstOffset, int length) throws BufferUnderflowException {
    throw new NotImplementedException("this is a FileFastBufferImpl");

  }

  @Override
  public void get(long srcOffset, ByteBuffer dst, int dstOffset, int length)
      throws BufferUnderflowException {
    throw new NotImplementedException("this is a FileFastBufferImpl");
  }

  public void deleteFile() {
    // don't need to check exists
    if (fileStoringData != null) {
      fileStoringData.delete();
    }
  }

  @Override
  public void put(byte[] src) throws BufferOverflowException {
    put(src, 0, src.length);
  }

  @Override
  public void put(byte[] src, int offset, int length) throws BufferOverflowException {
    if (length > this.length) {
      throw new BufferOverflowException("dst' length " + length + " is larger than the file size "
          + this.length);
    }
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(fileStoringData, false);
      fos.write(src, offset, length);
    } catch (IOException e) {
      logger.error("can't write data to the file");
      throw new RuntimeException();
    } finally {
      try {
        fos.close();
      } catch (IOException e) {
        logger.error("failed to close file handler", e);
      }
    }
  }

  @Override
  public void put(ByteBuffer src) throws BufferOverflowException {
    if (src.remaining() > this.length) {
      throw new BufferOverflowException(
          "dst' length " + src.remaining() + " is larger than the file size "
              + this.length);
    }
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(fileStoringData, false);
      FileChannel fileChannel = fos.getChannel();
      fileChannel.write(src);
    } catch (IOException e) {
      logger.error("can't write data to the file");
      throw new RuntimeException();
    } finally {
      try {
        fos.close();
      } catch (IOException e) {
        logger.error("failed to close file handler", e);
      }
    }
  }

  @Override
  public void put(long dstOffset, byte[] src, int srcOffset, int length)
      throws BufferOverflowException {
    throw new NotImplementedException("this is a FileFastBufferImpl");
  }

  @Override
  public void put(ByteBuffer src, int srcOffset, int length) throws BufferOverflowException {
    throw new NotImplementedException("this is a FileFastBufferImpl");
  }

  @Override
  public void put(long dstOffset, ByteBuffer src, int srcOffset, int length)
      throws BufferOverflowException {
    throw new NotImplementedException("this is a FileFastBufferImpl");
  }

  @Override
  public long size() {
    return length;
  }

  @Override
  public byte[] array() {
    if (size() == 0) {
      return null;
    }
    try {
      byte[] temp = new byte[(int) size()];
      get(temp);
      return temp;
    } catch (BufferUnderflowException e) {
      logger.error("can't clone an array", e);
      return null;
    }
  }
}
