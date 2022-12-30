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

package py.connection.pool;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.struct.EndPoint;
import py.connection.pool.udp.detection.NetworkIoHealthChecker;

public class PyConnectionPoolImpl implements PyConnectionPool, ReconnectionHandler {
  public static final int DEFAULT_CHECK_CONNECTION_INTERVAL_MS = 20;
  private static final Logger logger = LoggerFactory.getLogger(PyConnectionPoolImpl.class);
  private final Thread recycleThread;
  private final Thread detectThread;
  private final BlockingQueue<ConnectionRequest> blockingQueue;
  private Map<EndPoint, PyChannelPool> mapEndPointToChannelPool;
  private int poolSizePerOneEndPoint;
  private int maxLastTimeForConnectionMs;
  private BootstrapBuilder builder;
  private Map<PyChannel, ConnectionListenerManager> mapChannelToListener;
  private volatile boolean stop;

  public PyConnectionPoolImpl(int poolSizePerOneEndPoint, int maxLastTimeForConnectionMs,
      BootstrapBuilder builder) {
    this.mapEndPointToChannelPool = new ConcurrentHashMap<>();
    this.blockingQueue = new LinkedBlockingQueue<>();
    this.poolSizePerOneEndPoint = poolSizePerOneEndPoint;
    this.maxLastTimeForConnectionMs = maxLastTimeForConnectionMs;
    this.builder = builder;
    this.mapChannelToListener = new ConcurrentHashMap<>();
    this.stop = false;

    recycleThread = new Thread("recycle-thread") {
      public void run() {
        try {
          while (!stop) {
            ConnectionRequest request = blockingQueue.take();
            if (request instanceof FakeConnectionRequest) {
              logger.warn("exit thread for recycling connection pool");
              break;
            }

            try {
              processReconnect(request);
            } catch (Exception e) {
              logger.warn("caught an exception when processing reconnecting", e);
            }
          }
        } catch (Throwable t) {
          logger.error("recycle thread exit", t);
        }
      }
    };
    recycleThread.start();

    detectThread = new Thread("detect-thread") {
      public void run() {
        try {
          detect();
        } catch (Throwable t) {
          logger.error("detect thread exit", t);
        }
      }
    };
    detectThread.start();
  }

  public void retrieveForUnitTest() {
    stop = false;
  }

  private void processDetectResult(PyChannelPool pool, boolean detectResult) {
    if (!detectResult) {
      logger.debug("close the pool information as {}", pool);
      for (PyChannel channel : pool.getAll()) {
        if (mapChannelToListener.get(channel) != null || channel.get() != null) {
          logger.warn("close the socket when host broken, channel={}", channel);
          blockingQueue.add(new ExitChannelRequest(channel));
        }
      }
      return;
    }

    for (PyChannel channel : pool.getAll()) {
      ConnectionListenerManager manager = mapChannelToListener.get(channel);
      if (manager != null) {
        if (manager.isConnecting()) {
          logger.info(
              "We are making connection, dedup the current connection attempt, and wait the last "
                  + "one complete. {}",
              manager);
        } else {
          blockingQueue.add(new InnerChannelRequest(channel));
        }
      } else {
        Channel tmp = channel.get();
        if (tmp == null || !tmp.isActive()) {
          blockingQueue.add(new InnerChannelRequest(channel));
        }
      }

    }
  }

  public void detect() throws Exception {
    while (!stop) {
      for (Entry<EndPoint, PyChannelPool> entry : mapEndPointToChannelPool.entrySet()) {
        PyChannelPool pool = entry.getValue();

        NetworkIoHealthChecker.INSTANCE.keepChecking(pool.getEndpoint().getHostName());
        boolean result = !NetworkIoHealthChecker.INSTANCE
            .isUnreachable(pool.getEndpoint().getHostName());

        processDetectResult(pool, result);
        logger.trace(
            "UDP-detection client just checks the status of the connection to {}. connection is {}",
            pool.getEndpoint(), result);
      }

      Thread.sleep(DEFAULT_CHECK_CONNECTION_INTERVAL_MS);
    }
  }

  protected void processReconnect(ConnectionRequest request) throws Exception {
    if (request instanceof ConnectionRequestResult) {
      logger.warn("ConnectionRequestResult: {}", request);
      PyChannel pyChannel = request.getPyChannel();
      ConnectionListenerManager manager = mapChannelToListener.remove(pyChannel);
      if (manager != null) {
        manager.notifyAllListeners();
        manager.setConnecting(false);
      }

      return;
    } else if (request instanceof ExitChannelRequest) {
      logger.warn("ExitChannelRequest: {}", request);

      PyChannel pyChannel = request.getPyChannel();
      Channel oldChannel = pyChannel.set(null);
      if (oldChannel != null && !pyChannel.isClosing()) {
        logger.warn("close the channel={}, connection={}", oldChannel, request.getPyChannel());
        pyChannel.setClosing(true);
        oldChannel.close().addListener((ChannelFutureListener) future -> {
          blockingQueue.add(new ReconnectAfterCloseDoneRequestResult(request));
        });
      }

      ConnectionListenerManager manager = mapChannelToListener.remove(request.getPyChannel());
      if (manager != null) {
        logger.warn("notify all listener: {} when host is reachable", request.getPyChannel());
        manager.notifyAllListeners();
      } else {
        logger.warn("ExitChannelRequest: manager is null");
      }

      return;
    } else if (request instanceof CloseChannelRequest) {
      logger.warn("CloseChannelRequest result: {}", request);

      PyChannel channel = request.getPyChannel();
      ConnectionListenerManager manager = mapChannelToListener.get(channel);
      if (manager != null) {
        logger.warn("when closing the socket, someone want to reconnect");
        return;
      } else {
        logger.warn("CloseChannelRequest: manager is null");
      }

      if (channel.getLastIoTime() + maxLastTimeForConnectionMs >= System.currentTimeMillis()) {
        logger.warn(
            "CloseChannelRequest: not close: current time:{} last io time:{} max last time:{}",
            System.currentTimeMillis(), channel.getLastIoTime(), maxLastTimeForConnectionMs);
        return;
      }

      PyChannel pyChannel = request.getPyChannel();
      Channel oldChannel = pyChannel.set(null);
      if (oldChannel != null && !pyChannel.isClosing()) {
        logger.warn("close old channel:{}, and try connection:{}", oldChannel, channel);
        pyChannel.setClosing(true);
        oldChannel.close().addListener((ChannelFutureListener) future -> {
          blockingQueue.add(new ReconnectAfterCloseDoneRequestResult(request));
        });
      }
      return;
    } else if (request instanceof ReconnectAfterCloseDoneRequestResult) {
      logger.warn("when channel close done, start to reconnect, result:{}", request);

      PyChannel pyChannel = request.getPyChannel();
      pyChannel.setClosing(false);
      ConnectionListenerManager manager = mapChannelToListener.get(pyChannel);
      if (manager != null && !manager.isConnecting()) {
        logger.warn("for now listener is null, should generate new listener");

        manager.setConnecting(true);
        EndPoint endPoint = pyChannel.getEndPoint();
        builder.build().connect(endPoint.getHostName(), endPoint.getPort()).addListener(manager);
      }
      return;
    }

    PyChannel pyChannel = request.getPyChannel();
    ConnectionListenerManager manager = mapChannelToListener.get(pyChannel);
    if (manager != null) {
      logger.info("the channel:{} is trying to connect, manager:{}", pyChannel, manager);
      if (!(request instanceof InnerChannelRequest)) {
        manager.add(request);
      }
      return;
    }

    Channel currentChannel = pyChannel.get();
    if (currentChannel != null && currentChannel.isActive()) {
      logger.warn("the channel:{} has been built", pyChannel);
      request.complete(currentChannel);
      return;
    }

    boolean unreachable = NetworkIoHealthChecker.INSTANCE
        .isUnreachable(pyChannel.getEndPoint().getHostName());
    if (unreachable) {
      logger.warn("the host:{} is not reachable", pyChannel);

      request.complete(null);
      return;
    }

    manager = new ConnectionListenerManager(pyChannel) {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        try {
          super.operationComplete(future);
        } finally {
          blockingQueue.add(new ConnectionRequestResult(request));
        }
      }
    };

    if (!(request instanceof InnerChannelRequest)) {
      manager.add(request);
    }

    mapChannelToListener.put(pyChannel, manager);

    currentChannel = pyChannel.set(null);
    if (currentChannel != null) {
      logger.warn("close old channel: {}, and try connection={}", currentChannel, pyChannel);
      pyChannel.setClosing(true);
      currentChannel.close().addListener((ChannelFutureListener) future -> {
        blockingQueue.add(new ReconnectAfterCloseDoneRequestResult(request));
      });
      return;
    } else {
      Validate.isTrue(!manager.isConnecting());
      logger.debug("try connection:{}", pyChannel);

      manager.setConnecting(true);
      EndPoint endPoint = pyChannel.getEndPoint();
      builder.build().connect(endPoint.getHostName(), endPoint.getPort()).addListener(manager);
    }

  }

  @Override
  public void close() {
    try {
      stop = true;
      detectThread.join();
    } catch (Exception e) {
      logger.warn("can not wait for detect thread exit", e);
    }

    try {
      blockingQueue.offer(new FakeConnectionRequest());
      recycleThread.join();

    } catch (Exception e) {
      logger.warn("can not wait for reconnect thread exit", e);
    }

    for (Map.Entry<EndPoint, PyChannelPool> entry : mapEndPointToChannelPool.entrySet()) {
      entry.getValue().close();
    }

    mapEndPointToChannelPool.clear();
  }

  public PyConnection get(EndPoint endPoint) {
    PyChannelPool channelPool = mapEndPointToChannelPool.get(endPoint);
    if (channelPool == null) {
      channelPool = new PyChannelPool(poolSizePerOneEndPoint, endPoint, this);
      PyChannelPool prevAssociatedChannelPool = mapEndPointToChannelPool
          .putIfAbsent(endPoint, channelPool);

      if (prevAssociatedChannelPool != null) {
        channelPool = prevAssociatedChannelPool;
      }
    }

    return new PyConnectionImpl(channelPool.get(endPoint), this);
  }

  @Override
  public void reconnect(ConnectionRequest connectionRequest) {
    EndPoint endPoint = connectionRequest.getPyChannel().getEndPoint();
    PyChannelPool pool = mapEndPointToChannelPool.get(endPoint);
    Validate.isTrue(pool != null);
    if (NetworkIoHealthChecker.INSTANCE.isUnreachable(endPoint.getHostName())) {
      logger.warn("there is no need to reconnect={}", endPoint);
      connectionRequest.complete(null);
      return;
    }

    Validate.isTrue(blockingQueue.add(connectionRequest));
  }

  public Map<EndPoint, PyChannelPool> getMapEndPointToChannelPool() {
    return mapEndPointToChannelPool;
  }

  public void setMapEndPointToChannelPool(Map<EndPoint, PyChannelPool> mapEndPointToChannelPool) {
    this.mapEndPointToChannelPool = mapEndPointToChannelPool;
  }

  public Map<PyChannel, ConnectionListenerManager> getMapChannelToListener() {
    return mapChannelToListener;
  }

  public void pauseForUnitTest() {
    stop = true;
  }

  class ConnectionRequestResult extends ConnectionRequest {
    public ConnectionRequestResult(ConnectionRequest request) {
      super(request.getPyChannel());
    }
  }

  class ReconnectAfterCloseDoneRequestResult extends ConnectionRequest {
    public ReconnectAfterCloseDoneRequestResult(ConnectionRequest request) {
      super(request.getPyChannel());
    }
  }

  class ExitChannelRequest extends ConnectionRequest {
    public ExitChannelRequest(PyChannel pyChannel) {
      super(pyChannel);
    }
  }

  class CloseChannelRequest extends ConnectionRequest {
    public CloseChannelRequest(PyChannel pyChannel) {
      super(pyChannel);
    }
  }

  class InnerChannelRequest extends ConnectionRequest {
    public InnerChannelRequest(PyChannel pyChannel) {
      super(pyChannel);
    }
  }

  class FakeConnectionRequest extends ConnectionRequest {
    public FakeConnectionRequest() {
      super(null);
    }
  }
}
