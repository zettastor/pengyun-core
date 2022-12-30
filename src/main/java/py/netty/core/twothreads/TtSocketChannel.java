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

import static io.netty.channel.internal.ChannelUtils.MAX_BYTES_PER_GATHERING_WRITE_ATTEMPTED_LOW_THRESHOLD;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.FileRegion;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.socket.DefaultSocketChannelConfig;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SocketUtils;
import io.netty.util.internal.UnstableApi;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.Executor;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TtSocketChannel extends AbstractNioByteChannel implements
    io.netty.channel.socket.SocketChannel {
  private static final Logger logger = LoggerFactory.getLogger(TtSocketChannel.class);
  private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
  private final SocketChannelConfig config;

  public TtSocketChannel() {
    this(DEFAULT_SELECTOR_PROVIDER);
  }

  public TtSocketChannel(SelectorProvider provider) {
    this(newSocket(provider));
  }

  public TtSocketChannel(SocketChannel socket) {
    this(null, socket);
  }

  public TtSocketChannel(Channel parent, SocketChannel socket) {
    super(parent, socket);
    config = new NioSocketChannelConfig(this, socket.socket());
  }

  private static SocketChannel newSocket(SelectorProvider provider) {
    try {
      return provider.openSocketChannel();
    } catch (IOException e) {
      throw new ChannelException("Failed to open a socket.", e);
    }
  }

  private static void shutdownDone(ChannelFuture shutdownOutputFuture,
      ChannelFuture shutdownInputFuture,
      ChannelPromise promise) {
    Throwable shutdownOutputCause = shutdownOutputFuture.cause();
    Throwable shutdownInputCause = shutdownInputFuture.cause();
    if (shutdownOutputCause != null) {
      if (shutdownInputCause != null) {
        logger.debug("Exception suppressed because a previous exception occurred.",
            shutdownInputCause);
      }
      promise.setFailure(shutdownOutputCause);
    } else if (shutdownInputCause != null) {
      promise.setFailure(shutdownInputCause);
    } else {
      promise.setSuccess();
    }
  }

  @Override
  public ServerSocketChannel parent() {
    return (ServerSocketChannel) super.parent();
  }

  @Override
  public SocketChannelConfig config() {
    return config;
  }

  @Override
  protected SocketChannel javaChannel() {
    return (SocketChannel) super.javaChannel();
  }

  @Override
  public boolean isActive() {
    SocketChannel ch = javaChannel();
    return ch.isOpen() && ch.isConnected();
  }

  @Override
  public boolean isOutputShutdown() {
    return javaChannel().socket().isOutputShutdown() || !isActive();
  }

  @Override
  public boolean isInputShutdown() {
    return javaChannel().socket().isInputShutdown() || !isActive();
  }

  @Override
  public boolean isShutdown() {
    Socket socket = javaChannel().socket();
    return socket.isInputShutdown() && socket.isOutputShutdown() || !isActive();
  }

  @Override
  public InetSocketAddress localAddress() {
    return (InetSocketAddress) super.localAddress();
  }

  @Override
  public InetSocketAddress remoteAddress() {
    return (InetSocketAddress) super.remoteAddress();
  }

  @UnstableApi
  @Override
  protected final void doShutdownOutput() throws Exception {
    if (PlatformDependent.javaVersion() >= 7) {
      javaChannel().shutdownOutput();
    } else {
      javaChannel().socket().shutdownOutput();
    }
  }

  @Override
  public ChannelFuture shutdownOutput() {
    return shutdownOutput(newPromise());
  }

  @Override
  public ChannelFuture shutdownOutput(final ChannelPromise promise) {
    final TtEventLoop loop = eventLoop();
    if (loop.inEventLoop()) {
      ((AbstractUnsafe) unsafe()).shutdownOutput(promise);
    } else {
      loop.execute(new Runnable() {
        @Override
        public void run() {
          ((AbstractUnsafe) unsafe()).shutdownOutput(promise);
        }
      });
    }
    return promise;
  }

  @Override
  public ChannelFuture shutdownInput() {
    return shutdownInput(newPromise());
  }

  @Override
  public ChannelFuture shutdownInput(final ChannelPromise promise) {
    EventLoop loop = eventLoop();
    if (loop.inEventLoop()) {
      shutdownInput0(promise);
    } else {
      loop.execute(new Runnable() {
        @Override
        public void run() {
          shutdownInput0(promise);
        }
      });
    }
    return promise;
  }

  @Override
  protected boolean isInputShutdown0() {
    return isInputShutdown();
  }

  @Override
  public ChannelFuture shutdown() {
    return shutdown(newPromise());
  }

  @Override
  public ChannelFuture shutdown(final ChannelPromise promise) {
    ChannelFuture shutdownOutputFuture = shutdownOutput();
    if (shutdownOutputFuture.isDone()) {
      shutdownOutputDone(shutdownOutputFuture, promise);
    } else {
      shutdownOutputFuture.addListener(new ChannelFutureListener() {
        @Override
        public void operationComplete(final ChannelFuture shutdownOutputFuture) throws Exception {
          shutdownOutputDone(shutdownOutputFuture, promise);
        }
      });
    }
    return promise;
  }

  private void shutdownOutputDone(final ChannelFuture shutdownOutputFuture,
      final ChannelPromise promise) {
    ChannelFuture shutdownInputFuture = shutdownInput();
    if (shutdownInputFuture.isDone()) {
      shutdownDone(shutdownOutputFuture, shutdownInputFuture, promise);
    } else {
      shutdownInputFuture.addListener(new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture shutdownInputFuture) throws Exception {
          shutdownDone(shutdownOutputFuture, shutdownInputFuture, promise);
        }
      });
    }
  }

  private void shutdownInput0(final ChannelPromise promise) {
    try {
      shutdownInput0();
      promise.setSuccess();
    } catch (Throwable t) {
      promise.setFailure(t);
    }
  }

  private void shutdownInput0() throws Exception {
    if (PlatformDependent.javaVersion() >= 7) {
      javaChannel().shutdownInput();
    } else {
      javaChannel().socket().shutdownInput();
    }
  }

  @Override
  protected SocketAddress localAddress0() {
    return javaChannel().socket().getLocalSocketAddress();
  }

  @Override
  protected SocketAddress remoteAddress0() {
    return javaChannel().socket().getRemoteSocketAddress();
  }

  @Override
  protected void doBind(SocketAddress localAddress) throws Exception {
    doBind0(localAddress);
  }

  private void doBind0(SocketAddress localAddress) throws Exception {
    if (PlatformDependent.javaVersion() >= 7) {
      SocketUtils.bind(javaChannel(), localAddress);
    } else {
      SocketUtils.bind(javaChannel().socket(), localAddress);
    }
  }

  @Override
  protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
      throws Exception {
    if (localAddress != null) {
      doBind0(localAddress);
    }

    boolean success = false;
    try {
      logger.warn("connecting start");
      boolean connected = SocketUtils.connect(javaChannel(), remoteAddress);
      logger.warn("connecting end: connected:{} ", connected);
      if (!connected) {
        eventLoop().wakeupSelector();
        try {
          logger.warn("changing interest set ");
          selectionKey().interestOps(SelectionKey.OP_CONNECT);
          logger.warn("changed interest set ");
        } finally {
          eventLoop().waitSelectorDone();
        }
      }
      success = true;
      return connected;
    } catch (Throwable t) {
      logger.warn("caught a throwable when making connection to {} ", remoteAddress, t);
      return false;
    } finally {
      if (!success) {
        doClose();
      }
    }
  }

  @Override
  protected void doFinishConnect() throws Exception {
    eventLoop().wakeupSelector();
    boolean ok = false;
    try {
      logger.warn("finishing connection");
      ok = javaChannel().finishConnect();
      logger.warn("finished connection ");
    } finally {
      eventLoop().waitSelectorDone();
    }

    if (!ok) {
      throw new Error();
    }
  }

  @Override
  protected void doDisconnect() throws Exception {
    doClose();
  }

  @Override
  protected void doClose() throws Exception {
    super.doClose();
    javaChannel().close();
  }

  @Override
  protected int doReadBytes(ByteBuf byteBuf) throws Exception {
    final RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
    allocHandle.attemptedBytesRead(byteBuf.writableBytes());
    return byteBuf.writeBytes(javaChannel(), allocHandle.attemptedBytesRead());
  }

  @Override
  protected int doWriteBytes(ByteBuf buf) throws Exception {
    final int expectedWrittenBytes = buf.readableBytes();
    return buf.readBytes(javaChannel(), expectedWrittenBytes);
  }

  @Override
  protected long doWriteFileRegion(FileRegion region) throws Exception {
    final long position = region.transferred();
    return region.transferTo(javaChannel(), position);
  }

  private void adjustMaxBytesPerGatheringWrite(int attempted, int written,
      int oldMaxBytesPerGatheringWrite) {
    if (attempted == written) {
      if (attempted << 1 > oldMaxBytesPerGatheringWrite) {
        ((NioSocketChannelConfig) config).setMaxBytesPerGatheringWrite(attempted << 1);
      }
    } else if (attempted > MAX_BYTES_PER_GATHERING_WRITE_ATTEMPTED_LOW_THRESHOLD
        && written < attempted >>> 1) {
      ((NioSocketChannelConfig) config).setMaxBytesPerGatheringWrite(attempted >>> 1);
    }
  }

  @Override
  protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    SocketChannel ch = javaChannel();
    int totalBytes = 0;
    try {
      do {
        if (in.isEmpty()) {
          return;
        }

        int maxBytesPerGatheringWrite = ((NioSocketChannelConfig) config)
            .getMaxBytesPerGatheringWrite();

        ByteBuffer[] nioBuffers = in.nioBuffers(1024, maxBytesPerGatheringWrite);
        int nioBufferCnt = in.nioBufferCount();

        switch (nioBufferCnt) {
          case 0:

            Validate.isTrue(false, "should not be here");

            break;
          case 1: {
            final ByteBuffer buffer = nioBuffers[0];
            int attemptedBytes = buffer.remaining();
            final int localWrittenBytes = ch.write(buffer);

            totalBytes += localWrittenBytes;
            if (localWrittenBytes <= 0) {
              incompleteWrite(true);
              return;
            }

            adjustMaxBytesPerGatheringWrite(attemptedBytes, localWrittenBytes,
                maxBytesPerGatheringWrite);
            in.removeBytes(localWrittenBytes);
            break;
          }
          default: {
            final long localWrittenBytes = ch.write(nioBuffers, 0, nioBufferCnt);
            totalBytes += localWrittenBytes;

            if (localWrittenBytes <= 0) {
              incompleteWrite(true);
              return;
            }

            long attemptedBytes = in.nioBufferSize();

            adjustMaxBytesPerGatheringWrite((int) attemptedBytes, (int) localWrittenBytes,
                maxBytesPerGatheringWrite);
            in.removeBytes(localWrittenBytes);

            break;
          }
        }
      } while (true);
    } finally {
      logger.info("nothing need to do here");
    }
  }

  @Override
  protected AbstractNioUnsafe newUnsafe() {
    return new NioSocketChannelUnsafe();
  }

  @Override
  protected boolean isCompatible(EventLoop loop) {
    return loop instanceof TtEventLoop;
  }

  private final class NioSocketChannelUnsafe extends NioByteUnsafe {
    @Override
    protected Executor prepareToClose() {
      try {
        if (javaChannel().isOpen() && config().getSoLinger() > 0) {
          doDeregister();
          return GlobalEventExecutor.INSTANCE;
        }
      } catch (Throwable ignore) {
        // do nothing
      }
      return null;
    }
  }

  private final class NioSocketChannelConfig extends DefaultSocketChannelConfig {
    private volatile int maxBytesPerGatheringWrite = Integer.MAX_VALUE;

    private NioSocketChannelConfig(TtSocketChannel channel, Socket javaSocket) {
      super(channel, javaSocket);
      calculateMaxBytesPerGatheringWrite();
    }

    @Override
    protected void autoReadCleared() {
    }

    @Override
    public NioSocketChannelConfig setSendBufferSize(int sendBufferSize) {
      super.setSendBufferSize(sendBufferSize);
      calculateMaxBytesPerGatheringWrite();
      return this;
    }

    int getMaxBytesPerGatheringWrite() {
      return maxBytesPerGatheringWrite;
    }

    void setMaxBytesPerGatheringWrite(int maxBytesPerGatheringWrite) {
      this.maxBytesPerGatheringWrite = maxBytesPerGatheringWrite;
    }

    private void calculateMaxBytesPerGatheringWrite() {
      int newSendBufferSize = getSendBufferSize() << 1;
      if (newSendBufferSize > 0) {
        setMaxBytesPerGatheringWrite(getSendBufferSize() << 1);
      }
    }
  }
}
