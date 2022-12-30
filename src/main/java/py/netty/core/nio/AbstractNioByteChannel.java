/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package py.netty.core.nio;

import static io.netty.channel.internal.ChannelUtils.WRITE_STATUS_SNDBUF_FULL;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.FileRegion;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.internal.ChannelUtils;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.channel.socket.ChannelInputShutdownReadComplete;
import io.netty.util.internal.StringUtil;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public abstract class AbstractNioByteChannel extends AbstractNioChannel {
  private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
  private static final String EXPECTED_TYPES =
      " (expected: " + StringUtil.simpleClassName(ByteBuf.class) + ", "
          + StringUtil.simpleClassName(FileRegion.class) + ')';

  private Runnable flushTask;

  protected AbstractNioByteChannel(Channel parent, SelectableChannel ch) {
    super(parent, ch, SelectionKey.OP_READ);
  }

  protected abstract ChannelFuture shutdownInput();

  protected boolean isInputShutdown0() {
    return false;
  }

  @Override
  protected AbstractNioUnsafe newUnsafe() {
    return new NioByteUnsafe();
  }

  @Override
  public ChannelMetadata metadata() {
    return METADATA;
  }

  protected final int doWrite0(ChannelOutboundBuffer in) throws Exception {
    Object msg = in.current();
    if (msg == null) {
     
      return 0;
    }
    return doWriteInternal(in, in.current());
  }

  private int doWriteInternal(ChannelOutboundBuffer in, Object msg) throws Exception {
    if (msg instanceof ByteBuf) {
      ByteBuf buf = (ByteBuf) msg;
      if (!buf.isReadable()) {
        in.remove();
        return 0;
      }

      final int localFlushedAmount = doWriteBytes(buf);
      if (localFlushedAmount > 0) {
        in.progress(localFlushedAmount);
        if (!buf.isReadable()) {
          in.remove();
        }
        return 1;
      }
    } else if (msg instanceof FileRegion) {
      FileRegion region = (FileRegion) msg;
      if (region.transferred() >= region.count()) {
        in.remove();
        return 0;
      }

      long localFlushedAmount = doWriteFileRegion(region);
      if (localFlushedAmount > 0) {
        in.progress(localFlushedAmount);
        if (region.transferred() >= region.count()) {
          in.remove();
        }
        return 1;
      }
    } else {
     
      throw new Error();
    }
    return WRITE_STATUS_SNDBUF_FULL;
  }

  @Override
  protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    int writeSpinCount = config().getWriteSpinCount();
    do {
      Object msg = in.current();
      if (msg == null) {
       
        clearOpWrite();
       
        return;
      }
      writeSpinCount -= doWriteInternal(in, msg);
    } while (writeSpinCount > 0);

    incompleteWrite(writeSpinCount < 0);
  }

  @Override
  protected final Object filterOutboundMessage(Object msg) {
    if (msg instanceof ByteBuf) {
      ByteBuf buf = (ByteBuf) msg;
      if (buf.isDirect()) {
        return msg;
      }

      return newDirectBuffer(buf);
    }

    if (msg instanceof FileRegion) {
      return msg;
    }

    throw new UnsupportedOperationException(
        "unsupported message type: " + StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
  }

  protected final void incompleteWrite(boolean setOpWrite) {
   
    if (setOpWrite) {
      setOpWrite();
    } else {
     
      Runnable flushTask = this.flushTask;
      if (flushTask == null) {
        flushTask = this.flushTask = new Runnable() {
          @Override
          public void run() {
            flush();
          }
        };
      }
      eventLoop().execute(flushTask);
    }
  }

  protected abstract long doWriteFileRegion(FileRegion region) throws Exception;

  protected abstract int doReadBytes(ByteBuf buf) throws Exception;

  protected abstract int doWriteBytes(ByteBuf buf) throws Exception;

  protected final void setOpWrite() {
    final SelectionKey key = selectionKey();
   
   
   
    if (!key.isValid()) {
      return;
    }
    final int interestOps = key.interestOps();
    if ((interestOps & SelectionKey.OP_WRITE) == 0) {
      key.interestOps(interestOps | SelectionKey.OP_WRITE);
    }
  }

  protected final void clearOpWrite() {
    final SelectionKey key = selectionKey();
   
   
   
    if (!key.isValid()) {
      return;
    }
    final int interestOps = key.interestOps();
    if ((interestOps & SelectionKey.OP_WRITE) != 0) {
      key.interestOps(interestOps & ~SelectionKey.OP_WRITE);
    }
  }

  protected class NioByteUnsafe extends AbstractNioUnsafe {
    private void closeOnRead(ChannelPipeline pipeline) {
      if (!isInputShutdown0()) {
        if (Boolean.TRUE.equals(config().getOption(ChannelOption.ALLOW_HALF_CLOSURE))) {
          shutdownInput();
          pipeline.fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
        } else {
          close(voidPromise());
        }
      } else {
        pipeline.fireUserEventTriggered(ChannelInputShutdownReadComplete.INSTANCE);
      }
    }

    private void handleReadException(ChannelPipeline pipeline, ByteBuf byteBuf, Throwable cause,
        boolean close,
        RecvByteBufAllocator.Handle allocHandle) {
      if (byteBuf != null) {
        if (byteBuf.isReadable()) {
          readPending = false;
          pipeline.fireChannelRead(byteBuf);
        } else {
          byteBuf.release();
        }
      }
      allocHandle.readComplete();
      pipeline.fireChannelReadComplete();
      pipeline.fireExceptionCaught(cause);
      if (close || cause instanceof IOException) {
        closeOnRead(pipeline);
      }
    }

    @Override
    public final void read() {
      final ChannelConfig config = config();
      final ChannelPipeline pipeline = pipeline();
      final ByteBufAllocator allocator = config.getAllocator();
      final RecvByteBufAllocator.Handle allocHandle = recvBufAllocHandle();
      allocHandle.reset(config);

      ByteBuf byteBuf = null;
      boolean close = false;
      try {
        do {
          byteBuf = allocHandle.allocate(allocator);
          allocHandle.lastBytesRead(doReadBytes(byteBuf));
          if (allocHandle.lastBytesRead() <= 0) {
           
            byteBuf.release();
            byteBuf = null;
            close = allocHandle.lastBytesRead() < 0;
            if (close) {
             
              readPending = false;
            }
            break;
          }

          allocHandle.incMessagesRead(1);
          readPending = false;
          pipeline.fireChannelRead(byteBuf);
          byteBuf = null;
        } while (allocHandle.continueReading());

        allocHandle.readComplete();
        pipeline.fireChannelReadComplete();

        if (close) {
          closeOnRead(pipeline);
        }
      } catch (Throwable t) {
        handleReadException(pipeline, byteBuf, t, close, allocHandle);
      } finally {
        if (!readPending && !config.isAutoRead()) {
          removeReadOp();
        }
      }
    }
  }
}
