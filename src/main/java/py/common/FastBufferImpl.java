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

import java.nio.ByteBuffer;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.exception.BufferOverflowException;
import py.exception.BufferUnderflowException;
import sun.nio.ch.DirectBuffer;

@SuppressWarnings("restriction")
public class FastBufferImpl implements FastBuffer {
  private static final Logger logger = LoggerFactory.getLogger(FastBufferImpl.class);
  /**
   * address of assessible memory address which is behind metadata address.
   */
  long accessibleMemAddress = 0L;

  /**
   * size of accessible memory size.LThis is one of the two persistent data in buffer.
   */
  private long accessibleMemSize = 0L;

  FastBufferImpl(long address, long size) {
    this.accessibleMemAddress = address + MetadataDescriptor.ACCESSIBLE_MEM_OFFESET;
    this.accessibleMemSize = size;
  }

  /**
   * Use this method to reuse the object of {@link FastBuffer} to alleviate effort of jvm to create
   * a new instance of a class and free it.
   */
  FastBufferImpl reuse(long address, long size) {
    this.accessibleMemAddress = address + MetadataDescriptor.ACCESSIBLE_MEM_OFFESET;
    this.accessibleMemSize = size;

    return this;
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
  public void get(long srcOffset, byte[] dst, int dstOffset, int length)
      throws BufferUnderflowException {
    if (srcOffset + length > accessibleMemSize) {
      throw new BufferUnderflowException(
          "dst' length " + dst.length + " is larger than the buffer size "
              + accessibleMemSize + ", offset: " + srcOffset);
    }

    DirectAlignedBufferAllocator
        .copyMemory(accessibleMemAddress + srcOffset, dst, dstOffset, length);
  }

  @Override
  public void get(ByteBuffer dst) throws BufferUnderflowException {
    get(dst, dst.position(), dst.remaining());
  }

  @Override
  public void get(ByteBuffer dst, int offset, int length) throws BufferUnderflowException {
    get(0, dst, offset, length);
  }

  @Override
  public void get(long srcOffset, ByteBuffer dst, int dstOffset, int length)
      throws BufferUnderflowException {
    if (srcOffset + length > accessibleMemSize) {
      throw new BufferUnderflowException(
          "dst' length " + length + " is larger than the buffer size:"
              + accessibleMemSize + ", offset: " + srcOffset);
    }

    if (dst.isDirect()) {
      DirectAlignedBufferAllocator
          .copyMemory(accessibleMemAddress + srcOffset, ((DirectBuffer) dst).address()
              + dstOffset, length);
    } else {
      Validate.isTrue(dst.hasArray());
      get(srcOffset, dst.array(), dst.arrayOffset() + dstOffset, length);
    }
  }

  @Override
  public void put(ByteBuffer src) throws BufferOverflowException {
    put(0, src, src.position(), src.remaining());
  }

  @Override
  public void put(ByteBuffer src, int srcOffset, int length) throws BufferOverflowException {
    put(0, src, srcOffset, length);
  }

  @Override
  public void put(long dstOffset, ByteBuffer src, int srcOffset, int length)
      throws BufferOverflowException {
    if (dstOffset + length > this.accessibleMemSize) {
      throw new BufferOverflowException("src' length " + length + " is larger than the buffer size "
          + this.accessibleMemSize + ", dstOffset: " + dstOffset);
    }

    if (src.isDirect()) {
      DirectAlignedBufferAllocator
          .copyMemory(((DirectBuffer) src).address() + srcOffset, accessibleMemAddress
              + dstOffset, length);
    } else {
      Validate.isTrue(src.hasArray());
      put(dstOffset, src.array(), src.arrayOffset() + srcOffset, length);
    }
  }

  @Override
  public void put(byte[] src) throws BufferOverflowException {
    put(src, 0, src.length);
  }

  @Override
  public void put(byte[] src, int offset, int length) throws BufferOverflowException {
    put(0, src, offset, length);
  }

  @Override
  public void put(long dstOffset, byte[] src, int offset, int length)
      throws BufferOverflowException {
    if (dstOffset + length > accessibleMemSize) {
      throw new BufferOverflowException("length " + length + " is larger than the buffer size "
          + accessibleMemSize + ", offset: " + dstOffset);
    }

    DirectAlignedBufferAllocator.copyMemory(src, offset, length, accessibleMemAddress + dstOffset);
  }

  @Override
  public long size() {
    return this.accessibleMemSize;
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
