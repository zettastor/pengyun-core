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

import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.ServerChannel;
import java.io.IOException;
import java.net.PortUnreachableException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNioMessageChannel extends AbstractNioChannel {
  boolean inputShutdown;

  protected AbstractNioMessageChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
    super(parent, ch, readInterestOp);
  }

  @Override
  protected AbstractNioUnsafe newUnsafe() {
    return new NioMessageUnsafe();
  }

  @Override
  protected void doBeginRead() throws Exception {
    if (inputShutdown) {
      return;
    }
    super.doBeginRead();
  }

  @Override
  protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    final SelectionKey key = selectionKey();
    final int interestOps = key.interestOps();

    for (; ; ) {
      Object msg = in.current();
      if (msg == null) {
        if ((interestOps & SelectionKey.OP_WRITE) != 0) {
          key.interestOps(interestOps & ~SelectionKey.OP_WRITE);
        }
        break;
      }
      try {
        boolean done = false;
        for (int i = config().getWriteSpinCount() - 1; i >= 0; i--) {
          if (doWriteMessage(msg, in)) {
            done = true;
            break;
          }
        }

        if (done) {
          in.remove();
        } else {
          if ((interestOps & SelectionKey.OP_WRITE) == 0) {
            key.interestOps(interestOps | SelectionKey.OP_WRITE);
          }
          break;
        }
      } catch (Exception e) {
        if (continueOnWriteError()) {
          in.remove(e);
        } else {
          throw e;
        }
      }
    }
  }

  protected boolean continueOnWriteError() {
    return false;
  }

  protected boolean closeOnReadError(Throwable cause) {
    return cause instanceof IOException
        && !(cause instanceof PortUnreachableException)
        && !(this instanceof ServerChannel);
  }

  protected abstract int doReadMessages(List<Object> buf) throws Exception;

  protected abstract boolean doWriteMessage(Object msg, ChannelOutboundBuffer in) throws Exception;

  private final class NioMessageUnsafe extends AbstractNioUnsafe {
    private final List<Object> readBuf = new ArrayList<Object>();

    @Override
    public void read() {
      assert eventLoop().inEventLoop();
      final ChannelConfig config = config();
      final ChannelPipeline pipeline = pipeline();
      final RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
      allocHandle.reset(config);

      boolean closed = false;
      Throwable exception = null;
      try {
        try {
          do {
            int localRead = doReadMessages(readBuf);
            if (localRead == 0) {
              break;
            }
            if (localRead < 0) {
              closed = true;
              break;
            }

            allocHandle.incMessagesRead(localRead);
          } while (allocHandle.continueReading());
        } catch (Throwable t) {
          exception = t;
        }

        int size = readBuf.size();
        for (int i = 0; i < size; i++) {
          readPending = false;
          pipeline.fireChannelRead(readBuf.get(i));
        }
        readBuf.clear();
        allocHandle.readComplete();
        pipeline.fireChannelReadComplete();

        if (exception != null) {
          closed = closeOnReadError(exception);

          pipeline.fireExceptionCaught(exception);
        }

        if (closed) {
          inputShutdown = true;
          if (isOpen()) {
            close(voidPromise());
          }
        }
      } finally {
        if (!readPending && !config.isAutoRead()) {
          removeReadOp();
        }
      }
    }
  }
}
