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

package py.netty.core.twothreads;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import py.common.struct.EndPoint;
import py.netty.core.ByteToMessageDecoder;
import py.netty.server.GenericAsyncServer;
import py.test.TestBase;

public class TtEventLoopTest extends TestBase {
  private TtEventGroup buildClient(EndPoint endPoint, AtomicBoolean connected) {
    Bootstrap bootstrap = new Bootstrap();
    TtEventGroup ioEventGroup = new TtEventGroup(Runtime.getRuntime().availableProcessors(),
        new DefaultThreadFactory("netty-client-io", false, Thread.NORM_PRIORITY));
    bootstrap.group(ioEventGroup).channel(TtSocketChannel.class)
        .option(ChannelOption.TCP_NODELAY, true)
        .option(ChannelOption.SO_KEEPALIVE, true)
        .handler(new ChannelInitializer<AbstractChannel>() {
          @Override
          protected void initChannel(AbstractChannel ch) throws Exception {
            ChannelPipeline p = ch.pipeline();
            p.addLast(new ByteToMessageDecoder(10000, PooledByteBufAllocator.DEFAULT));
          }
        });

    bootstrap.connect(endPoint.getHostName(), endPoint.getPort())
        .addListener(new GenericFutureListener<ChannelFuture>() {
          @Override
          public void operationComplete(ChannelFuture future) throws Exception {
            Channel channel = future.channel();
            if (future.isSuccess()) {
              logger.warn("connection successfully, channel={}", channel);
              connected.set(true);
            } else {
              fail("fail to connection");
            }
          }

        });
    return ioEventGroup;
  }

  private GenericAsyncServer buildServer(EndPoint endPoint, AtomicBoolean terminated)
      throws InterruptedException {
    ServerBootstrap bootStrap = new ServerBootstrap();
    TtEventGroup parentEventGroup = new TtEventGroup(1,
        new DefaultThreadFactory("netty-server-boss", false, Thread.NORM_PRIORITY));

    TtEventGroup ioEventGroup = new TtEventGroup(4,
        new DefaultThreadFactory("netty-server-io-worker", false, Thread.NORM_PRIORITY));
    bootStrap.group(parentEventGroup, ioEventGroup).channel(TtServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 100).childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.WRITE_SPIN_COUNT, 50)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childHandler(
            new ChannelInitializer<AbstractChannel>() {
              @Override
              protected void initChannel(AbstractChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();
                p.addLast(new ByteToMessageDecoder(10000, PooledByteBufAllocator.DEFAULT));
              }
            });

    ChannelFuture serverChannelFuture = bootStrap.bind(endPoint.getHostName(), endPoint.getPort())
        .sync();
    logger.warn("start to listen on port: {}", endPoint);
    return new GenericAsyncServer() {
      @Override
      public void shutdown() {
        try {
          if (parentEventGroup != null) {
            parentEventGroup.shutdownGracefully(1, 2, TimeUnit.SECONDS);
            parentEventGroup.awaitTermination(5000, TimeUnit.MILLISECONDS);
          }

          if (ioEventGroup != null) {
            ioEventGroup.shutdownGracefully(1, 2, TimeUnit.SECONDS);
            terminated.set(ioEventGroup.awaitTermination(5000, TimeUnit.MILLISECONDS));
          }
        } catch (InterruptedException e) {
          logger.warn("Caught an exception", e);
        }

        try {
          serverChannelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
          logger.warn("Caught an exception", e);
        }
      }
    };
  }

  @Test
  public void testShutdown() throws Exception {
    EndPoint endPoint = new EndPoint("127.0.0.1", 18881);
    AtomicBoolean terminated = new AtomicBoolean(false);
    final GenericAsyncServer server = buildServer(endPoint, terminated);
    AtomicBoolean connected = new AtomicBoolean(false);
    TtEventGroup clientGroup = buildClient(endPoint, connected);

    int times = 0;
    while (true) {
      if (!connected.get()) {
        Thread.sleep(1000);
        times++;
      } else {
        break;
      }

      if (times >= 10) {
        fail("it took too long to connect");
      }
    }

    logger.info("shutdown client first");
    clientGroup.shutdownGracefully(1, 2, TimeUnit.SECONDS);
    assertTrue("client io event loop can't terminate in 5 seconds ",
        clientGroup.awaitTermination(5000, TimeUnit.SECONDS));

    logger.info("shutdown server now ");
    
    server.shutdown();
    assertTrue(terminated.get());
  }
}
