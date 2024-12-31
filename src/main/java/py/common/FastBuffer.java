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

package py.common;

import java.nio.ByteBuffer;
import py.exception.BufferOverflowException;
import py.exception.BufferUnderflowException;

public interface FastBuffer {
  /**
   * This method transfers bytes from this buffer into the given destination array.
   *
   * @throws BufferUnderflowException If the size of this buffer is fewer than that of dst.
   */
  public void get(byte[] dst) throws BufferUnderflowException;

  /**
   * similar to get(byte []).
   *
   * @param offset the start position in the dst array
   */
  public void get(byte[] dst, int offset, int length) throws BufferUnderflowException;

  /**
   * transfer data from this buffer to {@link ByteBuffer}, if someone modify the position of the
   * {@link ByteBuffer}, you will get wrong data or get a exception.
   */
  public void get(ByteBuffer dst) throws BufferUnderflowException;

  /**
   * similar to get(ByteBuffer), this method has a advantage that you needn't worry about the
   * position of the {@link ByteBuffer} modified, because the offset and length has specified,
   * namely it is thread-safe for this {@link ByteBuffer}.
   */
  public void get(ByteBuffer dst, int dstOffset, int length) throws BufferUnderflowException;

  /**
   * similar to get(ByteBuffer, int, int),namely it is thread-safe for this {@link ByteBuffer}.
   */
  public void get(long srcOffset, ByteBuffer dst, int dstOffset, int length)
      throws BufferUnderflowException;

  /**
   * transfer bytes from this buffer to the given destination array. The difference between this API
   * and get(byte[], int, int) is an offset for this buffer can be specified. Data beginning with
   * myOffset is copied.
   */
  public void get(long srcOffset, byte[] dst, int dstOffset, int length)
      throws BufferUnderflowException;

  /**
   * This method transfers the bytes remaining in the given source buffer into this buffer. If there
   * are more bytes in the source buffer than in this buffer, then no bytes are transferred and a
   * BufferOverflowException is thrown.
   */
  public void put(byte[] src) throws BufferOverflowException;

  /**
   * similar to put(byte []).
   *
   * @param srcOffset the start position in the src array
   */
  public void put(byte[] src, int srcOffset, int length) throws BufferOverflowException;

  /**
   * similar to put(byte [], int, int).
   */
  public void put(long dstOffset, byte[] src, int srcOffset, int length)
      throws BufferOverflowException;

  /**
   * similar to put(byte[] src), if someone modify the position of the {@link ByteBuffer}, you will
   * get wrong data or get a exception, namely it is not thread-safe for this {@link ByteBuffer}.
   */
  public void put(ByteBuffer src) throws BufferOverflowException;

  /**
   * similar to put(ByteBuffer), this method has a advantage that you needn't worry about the
   * position of the {@link ByteBuffer} modified, because the offset and length has specified,
   * namely it is thread-safe for this {@link ByteBuffer}.
   */
  public void put(ByteBuffer src, int srcOffset, int length) throws BufferOverflowException;

  /**
   * similar to put(ByteBuffer, int, int), for this {@link ByteBuffer}.
   */
  public void put(long dstOffset, ByteBuffer src, int srcOffset, int length)
      throws BufferOverflowException;

  /**
   * the size of the buffer. The reason of returning long rather than int is that the first buffer
   * before bing sliced might be larger.
   */
  public long size();

  /**
   * get the array of bytes out.
   */
  public byte[] array();
}