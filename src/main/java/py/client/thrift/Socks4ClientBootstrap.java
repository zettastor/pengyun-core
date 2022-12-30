/*
 * Copyright (C) 2012-2013 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package py.client.thrift;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.FixedLengthFrameDecoder;

public class Socks4ClientBootstrap extends ClientBootstrap {
  static final String FRAME_DECODER = "frameDecoder";
  static final String HANDSHAKE = "handshake";

  private final InetSocketAddress socksProxyAddr;

  public Socks4ClientBootstrap(ChannelFactory channelFactory, InetSocketAddress socksProxyAddr) {
    super(channelFactory);
    this.socksProxyAddr = socksProxyAddr;
  }

  public Socks4ClientBootstrap(InetSocketAddress socksProxyAddr) {
    this.socksProxyAddr = socksProxyAddr;
    super.setPipeline(getPipeline());
  }

  @Override
  public ChannelPipelineFactory getPipelineFactory() {
    return new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline()
          throws Exception {
        final ChannelPipeline cp = Channels.pipeline();
        cp.addLast(FRAME_DECODER, new FixedLengthFrameDecoder(8));
        cp.addLast(HANDSHAKE,
            new Socks4HandshakeHandler(Socks4ClientBootstrap.super.getPipelineFactory()));
        return cp;
      }
    };
  }

  @Override
  public ChannelFuture connect(final SocketAddress remoteAddress) {
    if (!(remoteAddress instanceof InetSocketAddress)) {
      throw new IllegalArgumentException("expecting InetSocketAddress");
    }
    final SettableChannelFuture settableChannelFuture = new SettableChannelFuture();
    super.connect(socksProxyAddr).addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future)
          throws Exception {
        settableChannelFuture.setChannel(future.getChannel());
        if (future.isSuccess()) {
          socksConnect(future.getChannel(), (InetSocketAddress) remoteAddress)
              .addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture innerFuture)
                    throws Exception {
                  if (innerFuture.isSuccess()) {
                    settableChannelFuture.setSuccess();
                  } else {
                    settableChannelFuture.setFailure(innerFuture.getCause());
                  }
                }
              });
        } else {
          settableChannelFuture.setFailure(future.getCause());
        }
      }
    });
    return settableChannelFuture;
  }

  private ChannelFuture socksConnect(Channel channel, InetSocketAddress remoteAddress) {
    ChannelBuffer handshake = null;
    if ((remoteAddress.getAddress() == null && remoteAddress.getHostName() != null) || remoteAddress
        .getHostName().equals("localhost")) {
      handshake = SocksProtocols
          .createSock4aPacket(remoteAddress.getHostName(), remoteAddress.getPort());
    }
    if (remoteAddress.getAddress() != null) {
      handshake = SocksProtocols
          .createSocks4packet(remoteAddress.getAddress(), remoteAddress.getPort());
    }

    if (handshake == null) {
      throw new IllegalArgumentException("Invalid Address " + remoteAddress);
    }

    channel.write(handshake);
    return ((Socks4HandshakeHandler) channel.getPipeline().get("handshake")).getChannelFuture();
  }
}
