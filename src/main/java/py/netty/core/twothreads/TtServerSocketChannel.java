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

import io.netty.channel.ChannelException;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.DefaultServerSocketChannelConfig;
import io.netty.channel.socket.ServerSocketChannelConfig;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SocketUtils;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TtServerSocketChannel extends AbstractNioMessageChannel
    implements io.netty.channel.socket.ServerSocketChannel {
  private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
  private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();

  private static final Logger logger = LoggerFactory.getLogger(TtServerSocketChannel.class);
  private final ServerSocketChannelConfig config;

  public TtServerSocketChannel() {
    this(newSocket(DEFAULT_SELECTOR_PROVIDER));
  }

  public TtServerSocketChannel(SelectorProvider provider) {
    this(newSocket(provider));
  }

  public TtServerSocketChannel(ServerSocketChannel channel) {
    super(null, channel, SelectionKey.OP_ACCEPT);
    config = new NioServerSocketChannelConfig(this, javaChannel().socket());
  }

  private static ServerSocketChannel newSocket(SelectorProvider provider) {
    try {
      
      return provider.openServerSocketChannel();
    } catch (IOException e) {
      throw new ChannelException("Failed to open a server socket.", e);
    }
  }

  @Override
  public InetSocketAddress localAddress() {
    return (InetSocketAddress) super.localAddress();
  }

  @Override
  public ChannelMetadata metadata() {
    return METADATA;
  }

  @Override
  public ServerSocketChannelConfig config() {
    return config;
  }

  @Override
  public boolean isActive() {
    return javaChannel().socket().isBound();
  }

  @Override
  public InetSocketAddress remoteAddress() {
    return null;
  }

  @Override
  protected ServerSocketChannel javaChannel() {
    return (ServerSocketChannel) super.javaChannel();
  }

  @Override
  protected SocketAddress localAddress0() {
    return SocketUtils.localSocketAddress(javaChannel().socket());
  }

  @Override
  protected void doBind(SocketAddress localAddress) throws Exception {
    if (PlatformDependent.javaVersion() >= 7) {
      javaChannel().bind(localAddress, config.getBacklog());
    } else {
      javaChannel().socket().bind(localAddress, config.getBacklog());
    }
  }

  @Override
  protected void doClose() throws Exception {
    javaChannel().close();
  }

  @Override
  protected int doReadMessages(List<Object> buf) throws Exception {
    SocketChannel ch = SocketUtils.accept(javaChannel());

    try {
      if (ch != null) {
        buf.add(new TtSocketChannel(this, ch));
        return 1;
      }
    } catch (Throwable t) {
      logger.warn("Failed to create a new channel from an accepted socket.", t);

      try {
        ch.close();
      } catch (Throwable t2) {
        logger.warn("Failed to close a socket.", t2);
      }
    }

    return 0;
  }

  @Override
  protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
      throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void doFinishConnect() throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  protected SocketAddress remoteAddress0() {
    return null;
  }

  @Override
  protected void doDisconnect() throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  protected boolean doWriteMessage(Object msg, ChannelOutboundBuffer in) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  protected final Object filterOutboundMessage(Object msg) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  protected boolean isCompatible(EventLoop loop) {
    return loop instanceof TtEventLoop;
  }

  private final class NioServerSocketChannelConfig extends DefaultServerSocketChannelConfig {
    private NioServerSocketChannelConfig(TtServerSocketChannel channel, ServerSocket javaSocket) {
      super(channel, javaSocket);
    }

    @Override
    protected void autoReadCleared() {
     
    }
  }
}
