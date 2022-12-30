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

package py.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.struct.EndPoint;
import py.consumer.ConsumerService;
import py.consumer.MultiThreadConsumerServiceWithBlockingQueue;
import py.netty.core.ByteToMessageDecoder;
import py.netty.core.IoEventThreadsMode;
import py.netty.core.IoEventUtils;
import py.netty.core.ProtocolFactory;
import py.netty.core.TransferenceConfiguration;
import py.netty.core.TransferenceOption;
import py.netty.core.twothreads.AbstractChannel;
import py.netty.core.twothreads.TtEventGroup;
import py.netty.core.twothreads.TtServerSocketChannel;
import py.netty.memory.PooledByteBufAllocatorWrapper;

public class GenericAsyncServerBuilder {
  public static final int MAX_IO_PENDING_REQUESTS = 1000;
  private static final Logger logger = LoggerFactory.getLogger(GenericAsyncServerBuilder.class);

  private final ConsumerService<Runnable> executorForProcessingRequests;

  private final EventLoopGroup parentEventGroup;

  private final EventLoopGroup ioEventGroup;
  private Object serviceObject;
  private ProtocolFactory protocolFactory;

  private TransferenceConfiguration configuration;
  private ChannelFuture serverChannelFuture;
  private int ioThreadCount;
  private int maxIoPendingRequests = MAX_IO_PENDING_REQUESTS;
  private ByteBufAllocator allocator = PooledByteBufAllocatorWrapper.INSTANCE;

  private py.common.Counter counterPendingRequests;

  public GenericAsyncServerBuilder(Object serviceObject, ProtocolFactory protocolFactory,
      TransferenceConfiguration configuration) {
    this(serviceObject, protocolFactory, configuration, "");
  }

  public GenericAsyncServerBuilder(Object serviceObject, ProtocolFactory protocolFactory,
      TransferenceConfiguration configuration, String prefix) {
    this.serviceObject = serviceObject;
    this.protocolFactory = protocolFactory;
    this.setConfiguration(configuration);

    parentEventGroup = new TtEventGroup(1,
        new DefaultThreadFactory("netty-server-boss-" + prefix, false,
            Thread.NORM_PRIORITY));

    ioThreadCount = IoEventUtils
        .calculateThreads((IoEventThreadsMode) getConfiguration()
                .valueOf(TransferenceServerOption.IO_EVENT_GROUP_THREADS_MODE),
            (Float) getConfiguration()
                .valueOf(TransferenceServerOption.IO_EVENT_GROUP_THREADS_PARAMETER));

    ioEventGroup = new TtEventGroup(ioThreadCount,
        new DefaultThreadFactory("netty-server-io-worker-" + prefix, false,
            Thread.NORM_PRIORITY));

    int numWorkerThreads = IoEventUtils
        .calculateThreads((IoEventThreadsMode) getConfiguration()
                .valueOf(TransferenceServerOption.IO_EVENT_HANDLE_THREADS_MODE),
            (Float) getConfiguration()
                .valueOf(TransferenceServerOption.IO_EVENT_HANDLE_THREADS_PARAMETER));
    this.executorForProcessingRequests = new MultiThreadConsumerServiceWithBlockingQueue<>(
        numWorkerThreads,
        Runnable::run, new LinkedBlockingQueue<>(), "netty-server-io-handler-" + prefix);
    this.executorForProcessingRequests.start();
  }

  public static TransferenceConfiguration defaultConfiguration() {
    TransferenceConfiguration configuration = TransferenceConfiguration.defaultConfiguration();
    configuration.option(TransferenceServerOption.IO_SERVER_SO_BACKLOG, 256);
    return configuration;
  }

  public GenericAsyncServer build(EndPoint endPoint) throws InterruptedException {
    int backLog = (int) getConfiguration().valueOf(TransferenceServerOption.IO_SERVER_SO_BACKLOG);
    logger.warn(">>>>server back log:{}<<<<", backLog);
    int maxFrameSize = (int) getConfiguration().valueOf(TransferenceOption.MAX_MESSAGE_LENGTH);

    protocolFactory.setByteBufAllocator(allocator);

    ServerBootstrap bootStrap = new ServerBootstrap();
    final AtomicInteger pendingIoRequests = new AtomicInteger(0);
    bootStrap.group(parentEventGroup, ioEventGroup).channel(TtServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, backLog).childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.WRITE_SPIN_COUNT, 50)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childHandler(new ChannelInitializer<AbstractChannel>() {
          @Override
          protected void initChannel(AbstractChannel ch) throws Exception {
            ByteToMessageDecoder byteToMessageDecoder = new ByteToMessageDecoder(maxFrameSize,
                allocator);

            ch.pipeline().addLast(byteToMessageDecoder);
            AsyncRequestHandler asyncRequestHandler = new AsyncRequestHandler(serviceObject,
                protocolFactory.getProtocol());
            asyncRequestHandler.setMaxPendingRequestsCount(maxIoPendingRequests);
            asyncRequestHandler.setPendingRequestsCount(pendingIoRequests);
            asyncRequestHandler.setExecutor(executorForProcessingRequests);

            ch.pipeline().addLast(asyncRequestHandler);
          }
        });

    bootStrap.childOption(ChannelOption.ALLOCATOR, allocator);
    int maxRcvBuf = (int) configuration.valueOf(TransferenceOption.MAX_BYTES_ONCE_ALLOCATE);

    bootStrap.childOption(ChannelOption.RCVBUF_ALLOCATOR,
        new AdaptiveRecvByteBufAllocator(512, 8192, maxRcvBuf));
    serverChannelFuture = bootStrap.bind(endPoint.getHostName(), endPoint.getPort()).sync();

    logger.warn("start to listen on port: {}, io thread count: {}", endPoint, ioThreadCount);
    return new GenericAsyncServer() {
      @Override
      public void shutdown() {
        logger.warn("stop to listen on port: {}", endPoint);

        try {
          if (parentEventGroup != null) {
            parentEventGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS);
            parentEventGroup.awaitTermination(6, TimeUnit.SECONDS);
          }

          if (ioEventGroup != null) {
            ioEventGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS);
            ioEventGroup.awaitTermination(6, TimeUnit.SECONDS);
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

  public void close() {
    if (executorForProcessingRequests != null) {
      executorForProcessingRequests.stop();
    }
  }

  public <T> GenericAsyncServerBuilder option(TransferenceServerOption<T> option, T value) {
    getConfiguration().option(option, value);
    return this;
  }

  public int getMaxIoPendingRequests() {
    return maxIoPendingRequests;
  }

  public void setMaxIoPendingRequests(int maxIoPendingRequests) {
    this.maxIoPendingRequests = maxIoPendingRequests;
  }

  public ByteBufAllocator getAllocator() {
    return allocator;
  }

  public void setAllocator(ByteBufAllocator allocator) {
    this.allocator = allocator;
  }

  public TransferenceConfiguration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(TransferenceConfiguration configuration) {
    this.configuration = configuration;
  }

  public void setCounterPendingRequests(py.common.Counter counterPendingRequests) {
    this.counterPendingRequests = counterPendingRequests;
  }
}
