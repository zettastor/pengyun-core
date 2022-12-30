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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.apache.thrift.transport.TTransportException;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientBossPool;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.NamedThreadFactory;
import py.common.rpc.share.ShutdownUtil;

public class NiftyClient implements Closeable {
  private final Logger logger = LoggerFactory.getLogger(NiftyClient.class);

  private final NettyClientConfig nettyClientConfig;
  private final HostAndPort defaultSocksProxyAddress;
  private final ChannelGroup allChannels = new DefaultChannelGroup();
  private NioClientBossPool bossPool = null;
  private NioWorkerPool workerPool = null;
  private NioClientSocketChannelFactory channelFactory;
  private ExecutionHandler executionHandler = null;
  private ThreadPoolExecutor responseExcecutor = null;

  public NiftyClient(NettyClientConfig nettyClientConfig) {
    logger.warn("NiftyClient construct now, pointer {}.", this);
    this.nettyClientConfig = nettyClientConfig;
    this.defaultSocksProxyAddress = nettyClientConfig.getDefaultSocksProxyAddress();

    bossPool = new NioClientBossPool(nettyClientConfig.getBossExecutor(),
        nettyClientConfig.getBossThreadCount(),
        nettyClientConfig.getTimer(), ThreadNameDeterminer.CURRENT);

    workerPool = new NioWorkerPool(nettyClientConfig.getWorkerExecutor(),
        nettyClientConfig.getWorkerThreadCount(),
        ThreadNameDeterminer.CURRENT);

    int minWorker = nettyClientConfig.getMinResponseThreadCount();
    if (minWorker > nettyClientConfig.getMaxResponseThreadCount()) {
      minWorker = nettyClientConfig.getMaxResponseThreadCount();
    }

    responseExcecutor = new ThreadPoolExecutor(minWorker,
        nettyClientConfig.getMaxResponseThreadCount(), 60L,
        TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
        new NamedThreadFactory("response-worker-"));

    responseExcecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
    executionHandler = new ExecutionHandler(responseExcecutor) {
      public void handleUpstream(ChannelHandlerContext context, ChannelEvent e) throws Exception {
        try {
          super.handleUpstream(context, e);
        } catch (Exception e1) {
          //
        }
      }
    };

    channelFactory = new NioClientSocketChannelFactory(bossPool, workerPool);
  }

  private static InetSocketAddress toInetAddress(HostAndPort hostAndPort) {
    return (hostAndPort == null) ? null
        : new InetSocketAddress(hostAndPort.getHost(), hostAndPort.getPort());
  }

  public NettyClientConfig getNettyClientConfig() {
    return nettyClientConfig;
  }

  public <T extends NiftyClientChannel> ListenableFuture<T> connectAsync(
      NiftyClientConnector<T> clientChannelConnector, int connectionTimeoutMs, int maxFrameSize) {
    return connectAsync(clientChannelConnector, connectionTimeoutMs, maxFrameSize,
        defaultSocksProxyAddress);
  }

  public <T extends NiftyClientChannel> ListenableFuture<T> connectAsync(
      NiftyClientConnector<T> clientChannelConnector, int connectionTimeoutMs, int maxFrameSize,
      @Nullable HostAndPort socksProxyAddress) {
    checkNotNull(clientChannelConnector, "clientChannelConnector is null");

    ClientBootstrap bootstrap = createClientBootstrap(socksProxyAddress);
    bootstrap.setOptions(nettyClientConfig.getBootstrapOptions());
    bootstrap.setOption("connectTimeoutMillis", connectionTimeoutMs);

    bootstrap.setPipelineFactory(
        clientChannelConnector.newChannelPipelineFactory(maxFrameSize, nettyClientConfig));
    ChannelFuture nettyChannelFuture = clientChannelConnector.connect(bootstrap);
    nettyChannelFuture.addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        Channel channel = future.getChannel();
        if (channel != null && channel.isOpen()) {
          allChannels.add(channel);
        }
      }
    });
    return new TaskNiftyFuture<>(clientChannelConnector, nettyChannelFuture);
  }

  public HostAndPort getDefaultSocksProxyAddress() {
    return defaultSocksProxyAddress;
  }

  public Iterator<Channel> getAllChannels() {
    return allChannels.iterator();
  }

  public void removeChannel(Channel channel) {
    allChannels.remove(channel);
  }

  @Override
  public void close() {
    ShutdownUtil.closeChannels(allChannels);
    logger.warn("go to stop thread pool start, pointer {}.", this);
    try {
      this.nettyClientConfig.getBossExecutor().shutdown();
      this.nettyClientConfig.getBossExecutor()
          .awaitTermination(nettyClientConfig.getCloseWaitTime(), TimeUnit.SECONDS);
      this.nettyClientConfig.getWorkerExecutor().shutdown();
      this.nettyClientConfig.getWorkerExecutor()
          .awaitTermination(nettyClientConfig.getCloseWaitTime(), TimeUnit.SECONDS);
    } catch (Exception e) {
      logger.error("awaitTermination exception happens.", e);
    }
    this.nettyClientConfig.getTimer().stop();
    logger.warn("go to stop thread pool end.");
    if (bossPool != null) {
      try {
        bossPool.shutdown();
      } catch (Exception e) {
        logger.warn("caught an exception", e);
      }
    }

    if (workerPool != null) {
      try {
        workerPool.shutdown();
      } catch (Exception e) {
        logger.warn("caught an exception", e);
      }
    }

    if (responseExcecutor != null) {
      try {
        ShutdownUtil.shutdownExecutor(responseExcecutor, "responseExcecutor");
      } catch (Exception e) {
        logger.warn("caught an exception", e);
      }
    }
  }

  private ClientBootstrap createClientBootstrap(@Nullable HostAndPort socksProxyAddress) {
    if (socksProxyAddress != null) {
      return new Socks4ClientBootstrap(channelFactory, toInetAddress(socksProxyAddress));
    } else {
      return new ClientBootstrap(channelFactory);
    }
  }

  private class TaskNiftyFuture<T extends NiftyClientChannel> extends AbstractFuture<T> {
    private TaskNiftyFuture(final NiftyClientConnector<T> clientChannelConnector,
        final ChannelFuture channelFuture) {
      channelFuture.addListener(new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
          try {
            if (future.isSuccess()) {
              Channel nettyChannel = future.getChannel();
              T channel = clientChannelConnector
                  .newThriftClientChannel(nettyChannel, nettyClientConfig,
                      executionHandler);
              set(channel);
            } else if (future.isCancelled()) {
              if (!cancel(true)) {
                logger.warn("cancel the connection request false {}", future.getChannel());
                setException(new TTransportException("Unable to cancel client channel connection"));
              } else {
                logger.warn("cancel the connection request true {}", future.getChannel());
              }
            } else {
              throw future.getCause();
            }
          } catch (Throwable t) {
            setException(new TTransportException("Failed to connect client channel", t));
          }
        }
      });
    }
  }
}
