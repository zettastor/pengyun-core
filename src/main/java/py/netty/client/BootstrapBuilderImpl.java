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

package py.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import py.connection.pool.AbstractBootstrapBuilder;
import py.connection.pool.udp.detection.NetworkIoHealthChecker;
import py.netty.core.ByteToMessageDecoder;
import py.netty.core.RequestTimeoutHandler;
import py.netty.core.TransferenceOption;
import py.netty.core.twothreads.AbstractChannel;
import py.netty.core.twothreads.TtSocketChannel;

public class BootstrapBuilderImpl extends AbstractBootstrapBuilder {
  private final ExecutorService executor;

  public BootstrapBuilderImpl(ExecutorService executor) {
    this.executor = executor;
  }

  @Override
  public Bootstrap build() {
    int ioTimeout = (int) cfg.valueOf(TransferenceClientOption.IO_TIMEOUT_MS);
    int maxFrameSize = (int) cfg.valueOf(TransferenceOption.MAX_MESSAGE_LENGTH);
    ChannelInitializer<AbstractChannel> initializer = new ChannelInitializer<AbstractChannel>() {
      @Override
      protected void initChannel(AbstractChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        MessageTimeManager messageTimeManager = new MessageTimeManager(hashedWheelTimer, ioTimeout);

        p.addLast(new ChannelInboundHandlerAdapter() {
          @Override
          public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            NetworkIoHealthChecker.INSTANCE
                .markReachableAndKeepChecking((InetSocketAddress) ctx.channel().remoteAddress());
            super.channelRead(ctx, msg);
          }
        });
        p.addLast(new ByteToMessageDecoder(maxFrameSize, allocator));

        p.addLast(new RequestTimeoutHandler(messageTimeManager));

        AsyncResponseHandler asyncResponseHandler = new AsyncResponseHandler(
            protocolFactory.getProtocol(),
            protocolFactory.getGetMethodTypeInterface(), messageTimeManager);
        asyncResponseHandler.setExecutor(executor);

        p.addLast(asyncResponseHandler);
      }
    };

    Bootstrap bootstrap = new Bootstrap();

    int maxRcvBuf = (int) cfg.valueOf(TransferenceOption.MAX_BYTES_ONCE_ALLOCATE);

    int connectionTimeout = (int) cfg.valueOf(TransferenceClientOption.IO_CONNECTION_TIMEOUT_MS);
    bootstrap.group(ioEventGroup).channel(TtSocketChannel.class)
        .option(ChannelOption.TCP_NODELAY, true)
        .option(ChannelOption.SO_KEEPALIVE, true)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout).handler(initializer);
    bootstrap.option(ChannelOption.ALLOCATOR, allocator);
    bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR,
        new AdaptiveRecvByteBufAllocator(512, 8192, maxRcvBuf));
    return bootstrap;
  }
}
