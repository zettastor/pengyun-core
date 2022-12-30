/*
 * Copyright (C) 2012-2013 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package py.client.thrift;

import com.google.common.net.HostAndPort;
import java.net.InetSocketAddress;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import py.common.rpc.share.TransDuplexProtocolFactory;

public class FramedClientConnector extends AbstractClientConnector<FramedClientChannel> {
  public static final int LENGTH_FIELD_LENGTH = 4;

  private static final int LENGTH_FIELD_OFFSET = 0;

  private static final int LENGTH_ADJUSTMENT = 0;

  private static final int INITIAL_BYTES_TO_STRIP = LENGTH_FIELD_LENGTH;

  private static final int DEFAULT_THRIFT_HEADER_SIZE = 1024;

  public FramedClientConnector(InetSocketAddress address) {
    this(address, defaultProtocolFactory());
  }

  public FramedClientConnector(HostAndPort address) {
    this(address, defaultProtocolFactory());
  }

  public FramedClientConnector(InetSocketAddress address,
      TransDuplexProtocolFactory protocolFactory) {
    super(address, protocolFactory);
  }

  public FramedClientConnector(HostAndPort address, TransDuplexProtocolFactory protocolFactory) {
    super(toSocketAddress(address), protocolFactory);
  }

  @Override
  public FramedClientChannel newThriftClientChannel(Channel nettyChannel,
      NettyClientConfig clientConfig,
      ExecutionHandler executionHandler) {
    FramedClientChannel channel = new FramedClientChannel(nettyChannel, clientConfig.getTimer(),
        getProtocolFactory(), clientConfig.getMaxChannelPendingSizeMb(),
        clientConfig.getMaxPendingRequestCount());
    ChannelPipeline cp = nettyChannel.getPipeline();
    TimeoutHandler.addToPipeline(cp);
    if (executionHandler != null) {
      cp.addLast("executor", executionHandler);
    }
    cp.addLast("thriftHandler", channel);
    return channel;
  }

  @Override
  public ChannelPipelineFactory newChannelPipelineFactory(final int maxFrameSize,
      NettyClientConfig clientConfig) {
    return new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline cp = Channels.pipeline();
        TimeoutHandler.addToPipeline(cp);

        cp.addLast("frameDecoder",
            new LengthFieldBasedFrameDecoder(maxFrameSize + DEFAULT_THRIFT_HEADER_SIZE,
                LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT,
                INITIAL_BYTES_TO_STRIP) {
              @Override
              public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
                  throws Exception {
                super.messageReceived(ctx, e);
              }
            });
        return cp;
      }
    };
  }
}
