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

package py.netty.core.twothreads;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ConnectTimeoutException;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import io.netty.util.internal.ThrowableUtil;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractNioChannel extends AbstractChannel {
  private static final Logger logger = LoggerFactory.getLogger(AbstractNioChannel.class);

  private static final ClosedChannelException DO_CLOSE_CLOSED_CHANNEL_EXCEPTION = ThrowableUtil
      .unknownStackTrace(new ClosedChannelException(), AbstractNioChannel.class, "doClose()");
  protected final int readInterestOp;
  private final SelectableChannel ch;
  protected volatile SelectionKey selectionKey;

  private ChannelPromise connectPromise;
  private ScheduledFuture<?> connectTimeoutFuture;
  private SocketAddress requestedRemoteAddress;

  protected AbstractNioChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
    super(parent);
    this.ch = ch;
    this.readInterestOp = readInterestOp;
    try {
      ch.configureBlocking(false);
    } catch (IOException e) {
      try {
        ch.close();
      } catch (IOException e2) {
        if (logger.isWarnEnabled()) {
          logger.warn("Failed to close a partially initialized socket.", e2);
        }
      }

      throw new ChannelException("Failed to enter non-blocking mode.", e);
    }
  }

  @Override
  public boolean isOpen() {
    return ch.isOpen();
  }

  @Override
  public NioUnsafe unsafe() {
    return (NioUnsafe) super.unsafe();
  }

  protected SelectableChannel javaChannel() {
    return ch;
  }

  @Override
  public TtEventLoop eventLoop() {
    return (TtEventLoop) super.eventLoop();
  }

  protected SelectionKey selectionKey() {
    assert selectionKey != null;
    return selectionKey;
  }

  @Override
  protected void doRegister() throws Exception {
    boolean selected = false;
    for (; ; ) {
      eventLoop().wakeupSelector();
      try {
        logger.warn("start to register {}", this);
        selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);
        logger.warn("completed registration {}", this);
        return;
      } catch (CancelledKeyException e) {
        if (!selected) {
          eventLoop().selectNow();
          selected = true;
        } else {
          throw e;
        }
      } finally {
        eventLoop().waitSelectorDone();
      }

    }
  }

  @Override
  protected void doDeregister() throws Exception {
    eventLoop().cancel(selectionKey());
  }

  @Override
  protected void doBeginRead() throws Exception {
    final SelectionKey selectionKey = this.selectionKey;
    if (!selectionKey.isValid()) {
      return;
    }

    eventLoop().wakeupSelector();
    try {
      final int interestOps = selectionKey.interestOps();
      if ((interestOps & readInterestOp) == 0) {
        selectionKey.interestOps(interestOps | readInterestOp);
      }
    } finally {
      eventLoop().waitSelectorDone();
    }
  }

  protected abstract boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
      throws Exception;

  protected abstract void doFinishConnect() throws Exception;

  protected final ByteBuf newDirectBuffer(ByteBuf buf) {
    final int readableBytes = buf.readableBytes();
    if (readableBytes == 0) {
      ReferenceCountUtil.safeRelease(buf);
      return Unpooled.EMPTY_BUFFER;
    }

    final ByteBufAllocator alloc = alloc();
    if (alloc.isDirectBufferPooled()) {
      ByteBuf directBuf = alloc.directBuffer(readableBytes);
      directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
      ReferenceCountUtil.safeRelease(buf);
      return directBuf;
    }

    final ByteBuf directBuf = ByteBufUtil.threadLocalDirectBuffer();
    if (directBuf != null) {
      directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
      ReferenceCountUtil.safeRelease(buf);
      return directBuf;
    }

    return buf;
  }

  protected final ByteBuf newDirectBuffer(ReferenceCounted holder, ByteBuf buf) {
    final int readableBytes = buf.readableBytes();
    if (readableBytes == 0) {
      ReferenceCountUtil.safeRelease(holder);
      return Unpooled.EMPTY_BUFFER;
    }

    final ByteBufAllocator alloc = alloc();
    if (alloc.isDirectBufferPooled()) {
      ByteBuf directBuf = alloc.directBuffer(readableBytes);
      directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
      ReferenceCountUtil.safeRelease(holder);
      return directBuf;
    }

    final ByteBuf directBuf = ByteBufUtil.threadLocalDirectBuffer();
    if (directBuf != null) {
      directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
      ReferenceCountUtil.safeRelease(holder);
      return directBuf;
    }

    if (holder != buf) {
      buf.retain();
      ReferenceCountUtil.safeRelease(holder);
    }

    return buf;
  }

  @Override
  protected void doClose() throws Exception {
    ChannelPromise promise = connectPromise;
    if (promise != null) {
      logger.warn("closing connectPromise {} ", connectPromise);

      promise.tryFailure(DO_CLOSE_CLOSED_CHANNEL_EXCEPTION);
      connectPromise = null;
    }

    ScheduledFuture<?> future = connectTimeoutFuture;
    if (future != null) {
      future.cancel(false);
      connectTimeoutFuture = null;
    }
  }

  public interface NioUnsafe extends Unsafe {
    SelectableChannel ch();

    void finishConnect();

    void read();

    void forceFlush();
  }

  protected abstract class AbstractNioUnsafe extends AbstractUnsafe implements NioUnsafe {
    protected final void removeReadOp() {
      throw new UnsupportedOperationException();
    }

    @Override
    public final SelectableChannel ch() {
      return javaChannel();
    }

    @Override
    public final void connect(final SocketAddress remoteAddress, final SocketAddress localAddress,
        final ChannelPromise promise) {
      assert eventLoop().inTaskThread();

      if (!promise.setUncancellable() || !ensureOpen(promise)) {
        return;
      }

      try {
        if (connectPromise != null) {
          throw new ConnectionPendingException();
        }

        boolean wasActive = isActive();
        if (doConnect(remoteAddress, localAddress)) {
          logger.debug("connected ");
          fulfillConnectPromise(promise, wasActive);
        } else {
          logger.debug("not connected yet");
          connectPromise = promise;
          requestedRemoteAddress = remoteAddress;

          int connectTimeoutMillis = config().getConnectTimeoutMillis();
          logger.debug("connection timeout is {} ms", connectTimeoutMillis);
          if (connectTimeoutMillis > 0) {
            connectTimeoutFuture = eventLoop().schedule(new Runnable() {
              @Override
              public void run() {
                logger.warn("connection timed out");
                ChannelPromise connectPromise = AbstractNioChannel.this.connectPromise;
                ConnectTimeoutException cause = new ConnectTimeoutException(
                    "connection timed out: " + remoteAddress);
                if (connectPromise != null && connectPromise.tryFailure(cause)) {
                  close(voidPromise());
                }
              }
            }, connectTimeoutMillis, TimeUnit.MILLISECONDS);
          }

          promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
              if (future.isCancelled()) {
                logger.debug("future {} is cancelled", future);
                if (connectTimeoutFuture != null) {
                  connectTimeoutFuture.cancel(false);
                }
                connectPromise = null;
                close(voidPromise());
              }
            }
          });
        }
      } catch (Throwable t) {
        promise.tryFailure(annotateConnectException(t, remoteAddress));
        closeIfClosed();
      }
    }

    private void fulfillConnectPromise(ChannelPromise promise, boolean wasActive) {
      logger.debug("promise {} ", promise);

      if (promise == null) {
        return;
      }

      boolean active = isActive();

      boolean promiseSet = promise.trySuccess();

      if (!wasActive && active) {
        pipeline().fireChannelActive();
      }

      if (!promiseSet) {
        close(voidPromise());
      }
    }

    private void fulfillConnectPromise(ChannelPromise promise, Throwable cause) {
      if (promise == null) {
        return;
      }

      promise.tryFailure(cause);
      closeIfClosed();
    }

    @Override
    public final void finishConnect() {
      logger.debug("finishConnect");
      TtEventLoop eventLoop = eventLoop();
      assert eventLoop.inEventLoop();
      assert eventLoop.inReaderThread();

      eventLoop.execute(new Runnable() {
        @Override
        public void run() {
          try {
            boolean wasActive = isActive();
            doFinishConnect();
            fulfillConnectPromise(connectPromise, wasActive);
          } catch (Throwable t) {
            fulfillConnectPromise(connectPromise,
                annotateConnectException(t, requestedRemoteAddress));
          } finally {
            if (connectTimeoutFuture != null) {
              connectTimeoutFuture.cancel(false);
            }
            connectPromise = null;
          }
        }
      });
    }

    @Override
    protected final void flush0() {
      eventLoop().inTaskThread(Thread.currentThread());

      if (isFlushPending()) {
        return;
      }
      super.flush0();
    }

    @Override
    public final void forceFlush() {
      eventLoop().inTaskThread();

      super.flush0();
    }

    private boolean isFlushPending() {
      return false;
    }

  }
}
