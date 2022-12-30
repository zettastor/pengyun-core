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

package py.storage.async;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.tlsf.bytebuffer.manager.TlsfByteBufferManager;
import py.common.tlsf.bytebuffer.manager.TlsfByteBufferManagerFactory;
import sun.nio.ch.DirectBuffer;

class IoUtil {
  private static final Logger logger = LoggerFactory.getLogger(IoUtil.class);

  private static <A> void writeFromNativeBuffer(ByteBuffer buffer, long offset,
      AsyncFileAccessor accessor,
      A attachment, CompletionHandler<Integer, A> completionHandler) {
    int length = buffer.remaining();
    int position = buffer.position();

    Validate.isTrue(length > 0);

    accessor.write(((DirectBuffer) buffer).address() + position, offset, length, attachment,
        completionHandler);
  }

  private static <A> void readIntoNativeBuffer(ByteBuffer buffer, long offset,
      AsyncFileAccessor accessor,
      A attachment, CompletionHandler<Integer, A> completionHandler) {
    int position = buffer.position();

    int length = buffer.remaining();
    if (length <= 0) {
      logger.warn("reading into a empty buffer {} {} {}", accessor, offset, buffer);
      completionHandler.completed(0, attachment);
    } else {
      accessor.read(((DirectBuffer) buffer).address() + (long) position, offset, length, attachment,
          new CompletionHandler<Integer, A>() {
            @Override
            public void completed(Integer result, A attachment) {
              if (result > 0) {
                buffer.position(position + result);
              }
              completionHandler.completed(result, attachment);
            }

            @Override
            public void failed(Throwable exc, A attachment) {
              completionHandler.failed(exc, attachment);
            }
          });
    }
  }

  static <A> void read(ByteBuffer buffer, long offset, AsyncFileAccessor accessor, A attachment,
      CompletionHandler<Integer, A> completionHandler) {
    if (buffer.isReadOnly()) {
      throw new IllegalArgumentException("Read-only buffer");
    } else if (buffer instanceof DirectBuffer) {
      readIntoNativeBuffer(buffer, offset, accessor, attachment, completionHandler);
    } else {
      TlsfByteBufferManager tlsfByteBufferManager = TlsfByteBufferManagerFactory.instance();
      Validate.notNull(tlsfByteBufferManager);
      ByteBuffer directBuffer = tlsfByteBufferManager.blockingAllocate(buffer.remaining());

      readIntoNativeBuffer(directBuffer, offset, accessor, attachment,
          new CompletionHandler<Integer, A>() {
            @Override
            public void completed(Integer result, A attachment) {
              directBuffer.flip();
              if (result > 0) {
                buffer.put(directBuffer);
              }
              tlsfByteBufferManager.release(directBuffer);
              completionHandler.completed(result, attachment);
            }

            @Override
            public void failed(Throwable exc, A attachment) {
              tlsfByteBufferManager.release(directBuffer);
              completionHandler.failed(exc, attachment);
            }
          });
    }
  }

  static <A> void write(ByteBuffer buffer, long offset, AsyncFileAccessor accessor, A attachment,
      CompletionHandler<Integer, A> completionHandler) {
    if (buffer instanceof DirectBuffer) {
      writeFromNativeBuffer(buffer, offset, accessor, attachment, completionHandler);
    } else {
      int position = buffer.position();
      int limit = buffer.limit();

      int length = position <= limit ? limit - position : 0;
      if (length == 0) {
        logger.warn("writing from a 0 size buffer {} {} {}", accessor, offset, buffer);
        completionHandler.completed(0, attachment);
        return;
      }

      TlsfByteBufferManager tlsfByteBufferManager = TlsfByteBufferManagerFactory.instance();
      Validate.notNull(tlsfByteBufferManager);

      ByteBuffer directBuffer = tlsfByteBufferManager.blockingAllocate(length);

      directBuffer.put(buffer);
      directBuffer.flip();

      buffer.position(position);
      writeFromNativeBuffer(directBuffer, offset, accessor, attachment,
          new CompletionHandler<Integer, A>() {
            @Override
            public void completed(Integer result, A attachment) {
              if (result > 0) {
                buffer.position(position + result);
              }
              tlsfByteBufferManager.release(directBuffer);
              completionHandler.completed(result, attachment);
            }

            @Override
            public void failed(Throwable exc, A attachment) {
              tlsfByteBufferManager.release(directBuffer);
              completionHandler.failed(exc, attachment);
            }
          });
    }
  }

}
