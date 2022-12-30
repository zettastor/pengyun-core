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

package py.client.thrift;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingTransport;
import org.apache.thrift.transport.TTransport;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.client.GenericProxyFactory;
import py.common.rpc.share.TransDuplexProtocolFactory;
import py.common.struct.EndPoint;
import py.exception.GenericThriftClientFactoryException;

@ThreadSafe
public class GenericThriftClientFactory<T> {
  public static final int DEFAULT_MAX_FRAME_SIZE = 16 * 1024 * 1024;
  private static final Logger logger = LoggerFactory.getLogger(GenericThriftClientFactory.class);
  private static final int DEFAULT_NUM_CHANNELS = 1;
  private static final int EXCEPTION_RECORD_PER_NUM = 200;
  private final Map<EndPoint, List<PyAsyncClientManager>> mapFromEndPointToThrift;
  private final TProtocolFactory asyncProtocolFactory;
  private final TProtocolFactory protocolFactory;
  private final PyReflectionHelper asyncMethodCallHelper;
  
  private final Map<EndPoint, Integer> mapFromEndPointToChannelCount;
  private final Map<EndPoint, AtomicInteger> mapFromEndPointToRoundRobin;
  private final Map<EndPoint, Object> mapEndPointToLock;
  private final Map<T, PyAsyncClientManager> mapClientToManager;
  private NiftyClient niftyClient;
  private Class<T> serviceInterface;
  private Constructor<T> asyncConstructor;
  private Constructor<T> syncConstructor;
  private String callerClassName;
  private int defaultSocketTimeoutMs = 10000;
  private int defaultConnectionTimeoutMs = 3000;
  private int exceptionCounter = 0;
  
  private int maxNetworkFrameSize = DEFAULT_MAX_FRAME_SIZE;
  private boolean needCache = false;

  protected GenericThriftClientFactory(Class<T> serviceInterface, TProtocolFactory factory,
      int numWorkerThreadCount,
      int minResponseThreadCount, int maxResponseThreadCount, boolean closeSafe) {
    this.mapFromEndPointToThrift = new ConcurrentHashMap<>();
    this.mapFromEndPointToChannelCount = new ConcurrentHashMap<>();
    this.mapFromEndPointToRoundRobin = new HashMap<>();
    this.mapEndPointToLock = new HashMap<>();
    this.mapClientToManager = new ConcurrentHashMap<>();

    try {
      asyncMethodCallHelper = new PyReflectionHelper();
    } catch (Exception e) {
      logger.error("cannot initiate the interface:{}", serviceInterface.getName());
      throw new RuntimeException();
    }

    this.serviceInterface = checkServiceInterface(serviceInterface);
    logger.debug("service interface is {}", this.serviceInterface.getName());

    this.protocolFactory = factory;
    this.asyncProtocolFactory = new TProtocolFactory() {
      private static final long serialVersionUID = 1L;

      @Override
      public TProtocol getProtocol(TTransport trans) {
        return new PyProtocolProxy(protocolFactory.getProtocol(trans));
      }
    };

    callerClassName = serviceInterface.getSimpleName();
    String serviceName = serviceInterface.getEnclosingClass().getSimpleName();
    NettyClientConfigBuilder configBuilder = NettyClientConfig.newBuilder();
    configBuilder.setBossThreadCount(1);
    configBuilder.setNiftyName(serviceName);
    if (maxResponseThreadCount > 0) {
      configBuilder.setMaxResponseThreadCount(maxResponseThreadCount);
    }
    if (minResponseThreadCount > 0) {
      configBuilder.setMinResponseThreadCount(minResponseThreadCount);
    }
    if (numWorkerThreadCount > 0) {
      configBuilder.setWorkerThreadCount(numWorkerThreadCount);
    }

    if (!closeSafe) {
      configBuilder.setCloseWaitTime(0);
    }

    niftyClient = new NiftyClient(configBuilder.build());
  }

  public static <T> GenericThriftClientFactory<T> create(Class<T> serviceInterface) {
    return create(serviceInterface, new TCompactProtocol.Factory(), 0, 0);
  }

  public static <T> GenericThriftClientFactory<T> create(Class<T> serviceInterface,
      boolean closeSafe) {
    return new GenericThriftClientFactory<T>(serviceInterface, new TCompactProtocol.Factory(), 0,
        0, 0, closeSafe);
  }

  public static <T> GenericThriftClientFactory<T> create(Class<T> serviceInterface,
      int minResponseThreadCount) {
    return create(serviceInterface, new TCompactProtocol.Factory(), minResponseThreadCount, 0);
  }

  public static <T> GenericThriftClientFactory<T> create(Class<T> serviceInterface,
      int minResponseThreadCount,
      int maxResponseThreadCount) {
    return new GenericThriftClientFactory<T>(serviceInterface, new TCompactProtocol.Factory(), 0,
        minResponseThreadCount, maxResponseThreadCount, true);
  }

  public static <T> GenericThriftClientFactory<T> create(Class<T> serviceInterface,
      TProtocolFactory protocolFactory,
      int minResponseThreadCount, int maxResponseThreadCount) {
    return new GenericThriftClientFactory<T>(serviceInterface, protocolFactory, 0,
        minResponseThreadCount,
        maxResponseThreadCount, true);
  }

  public static <T> GenericThriftClientFactory<T> create(Class<T> serviceInterface,
      TProtocolFactory protocolFactory,
      int numWorkerThreadCount, int minResponseThreadCount, int maxResponseThreadCount) {
    return new GenericThriftClientFactory<T>(serviceInterface, protocolFactory,
        numWorkerThreadCount,
        minResponseThreadCount, maxResponseThreadCount, true);
  }

  static <I> Class<I> checkServiceInterface(Class<I> serviceInterface) {
    Preconditions.checkNotNull(serviceInterface);
    Preconditions
        .checkArgument(serviceInterface.isInterface(), "%s must be a thrift service interface",
            serviceInterface);
    return serviceInterface;
  }

  private static <T> Constructor<? extends T> findAsyncImplementationConstructor(
      final Class<T> serviceInterface) {
    Class<? extends T> implementationClass = findImplementationClass(serviceInterface);
    try {
      return implementationClass
          .getConstructor(TProtocolFactory.class, TAsyncClientManager.class,
              TNonblockingTransport.class);
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(
          "Failed to find expected constructor " + "in service client class: "
              + implementationClass);
    }
  }

  private static <T> Constructor<? extends T> findImplementationConstructor(
      final Class<T> serviceInterface) {
    Class<? extends T> implementationClass = findImplementationClass(serviceInterface);
    try {
      return implementationClass.getConstructor(TProtocol.class, TProtocol.class);
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(
          "Failed to find a single argument TProtocol constructor " + "in service client class: "
              + implementationClass);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> Class<? extends T> findImplementationClass(final Class<T> serviceInterface) {
    try {
      return (Class<? extends T>) Iterables
          .find(ImmutableList.copyOf(serviceInterface.getEnclosingClass().getClasses()),
              new Predicate<Class<?>>() {
                @Override
                public boolean apply(Class<?> inner) {
                  return !serviceInterface.equals(inner) && serviceInterface
                      .isAssignableFrom(inner);
                }
              });
    } catch (NoSuchElementException e) {
      throw new IllegalArgumentException(
          "Could not find a sibling enclosed implementation of " + "service interface: "
              + serviceInterface);
    }
  }

  public void setNeedCache() {
    this.needCache = true;
  }

  public T generateAsyncClient(final EndPoint endPoint) throws GenericThriftClientFactoryException {
    return generateAsyncClient(endPoint, defaultSocketTimeoutMs);
  }

  public T generateAsyncClient(final EndPoint endPoint, long socketTimeoutMs)
      throws GenericThriftClientFactoryException {
    return generateAsyncClient(endPoint, socketTimeoutMs, defaultConnectionTimeoutMs);
  }

  @SuppressWarnings("unchecked")
  public T generateAsyncClient(final EndPoint endPoint, long socketTimeoutMs,
      int connectionTimeoutMs)
      throws GenericThriftClientFactoryException {
    try {
      PyAsyncClientManager manager = getAsyncManager(endPoint, connectionTimeoutMs);
      manager.setCallerClassName(callerClassName);

      Object[] intArgs = new Object[]{asyncProtocolFactory, manager, null};
      if (asyncConstructor == null) {
        asyncConstructor = (Constructor<T>) findAsyncImplementationConstructor(serviceInterface);
      }

      T client = (T) asyncConstructor.newInstance(intArgs);
      asyncMethodCallHelper.getTimeoutField().set(client, socketTimeoutMs);
      return client;

    } catch (Exception e) {
     
      if (++exceptionCounter % EXCEPTION_RECORD_PER_NUM == 0) {
        logger.warn("can not generate an async client for EndPoint: {}, exception occured times {}",
            endPoint,
            exceptionCounter);
      }
      throw new GenericThriftClientFactoryException(e);
    }
  }

  public T generateSyncClient(final EndPoint endPoint) throws GenericThriftClientFactoryException {
    return generateSyncClient(endPoint, defaultSocketTimeoutMs);
  }

  public T generateSyncClient(final EndPoint endPoint, long socketTimeoutMs)
      throws GenericThriftClientFactoryException {
    return generateSyncClient(endPoint, socketTimeoutMs, defaultConnectionTimeoutMs);
  }

  public T generateSyncClient(final EndPoint endPoint, long socketTimeoutMs, boolean delegatable)
      throws GenericThriftClientFactoryException {
    return generateSyncClient(endPoint, socketTimeoutMs, defaultConnectionTimeoutMs, delegatable);
  }

  public T generateSyncClient(final EndPoint endPoint, long socketTimeoutMs,
      int connectionTimeoutMs)
      throws GenericThriftClientFactoryException {
    return generateSyncClient(endPoint, socketTimeoutMs, connectionTimeoutMs, false);
  }

  @SuppressWarnings("unchecked")
  public T generateSyncClient(final EndPoint endPoint, long socketTimeoutMs,
      int connectionTimeoutMs,
      boolean delegatable) throws GenericThriftClientFactoryException {
    try {
      PyAsyncClientManager manager = getAsyncManager(endPoint, connectionTimeoutMs);
      T client;
      if (needCache) {
        client = (T) manager.get();
        if (client != null) {
          logger.debug("get from cache for endpoint={}", endPoint);
          return client;
        }
      }

      PySyncClientTransport transport = new PySyncClientTransport(manager, socketTimeoutMs,
          maxNetworkFrameSize);

      transport.setCallerClassName(callerClassName);
      PyProtocolProxy protocol = (PyProtocolProxy) asyncProtocolFactory.getProtocol(transport);
      transport.setProxy(protocol);
      Object[] intArgs = new Object[]{protocol, protocol};
      if (syncConstructor == null) {
        syncConstructor = (Constructor<T>) findImplementationConstructor(serviceInterface);
      }

      if (delegatable) {
        T rawClient = syncConstructor.newInstance(intArgs);
       
        GenericProxyFactory<T> clientProxyFactory = new GenericProxyFactory<T>(serviceInterface,
            rawClient);

        Class<TProtocol>[] types = new Class[]{TProtocol.class, TProtocol.class};
        client = clientProxyFactory.createJavassistDynamicProxy(types, intArgs);
      } else {
        client = syncConstructor.newInstance(intArgs);
      }

      transport.setClientObject(client);
      transport.setPyReflectionHelper(asyncMethodCallHelper);

      if (needCache) {
        mapClientToManager.put(client, manager);
      }
      return client;
    } catch (Exception e) {
      if (++exceptionCounter % EXCEPTION_RECORD_PER_NUM == 0) {
        logger.warn("can not generate an sync client for EndPoint: {}, exception counter {}",
            endPoint,
            exceptionCounter);
      }
      throw new GenericThriftClientFactoryException(
          "fail to connect to endpoint= " + endPoint + ", socket timeout=" + socketTimeoutMs
              + ", connection timeout=" + connectionTimeoutMs, e);
    }
  }

  private PyAsyncClientManager getAsyncManager(EndPoint endPoint, int connectionTimeoutMs)
      throws Exception {
    Integer channelCount = mapFromEndPointToChannelCount.get(endPoint);
    if (channelCount == null) {
      synchronized (this) {
        channelCount = mapFromEndPointToChannelCount.get(endPoint);
        if (channelCount == null) {
         
          setChannelCountToEndPoint(endPoint, DEFAULT_NUM_CHANNELS);
          channelCount = Integer.valueOf(DEFAULT_NUM_CHANNELS);
        }
      }
    }

    AtomicInteger roundRobinSeq = mapFromEndPointToRoundRobin.get(endPoint);
    int index = roundRobinSeq.getAndIncrement() % channelCount;

    List<PyAsyncClientManager> managers = mapFromEndPointToThrift.get(endPoint);
    PyAsyncClientManager manager = getManager(managers, index);

    if (manager != null && !manager.getChannel().getNettyChannel().isConnected()) {
      logger.debug("before synchronize, can not connect to service: {}, timeout: {}, index: {}",
          endPoint,
          connectionTimeoutMs, index);
      long startTime = System.currentTimeMillis();
      synchronized (mapEndPointToLock.get(endPoint)) {
        Channel oldChannel = manager.getChannel().getNettyChannel();
        if (!oldChannel.isConnected()) {
          int leftTime = connectionTimeoutMs - (int) (System.currentTimeMillis() - startTime);
          if (leftTime <= 0) {
            throw new GenericThriftClientFactoryException(
                "1 there is no enough time: " + connectionTimeoutMs);
          }

          logger.debug("after synchronize, can not connect to service: {}, timeout: {}, index: {}",
              endPoint,
              connectionTimeoutMs, index);
          NiftyClientChannel channel = generateChannel(endPoint.getHostName(), endPoint.getPort(),
              leftTime);
          manager.setChannel(channel);
          logger.debug("successful connect to service: {}", endPoint);
          oldChannel.close();
          niftyClient.removeChannel(oldChannel);
        }
      }
    }

    if (manager != null) {
      return manager;
    }

    long startTime = System.currentTimeMillis();
    synchronized (mapEndPointToLock.get(endPoint)) {
      managers = mapFromEndPointToThrift.get(endPoint);
      manager = getManager(managers, index);

      if (manager == null) {
        int leftTime = connectionTimeoutMs - (int) (System.currentTimeMillis() - startTime);
        if (leftTime <= 0) {
          throw new GenericThriftClientFactoryException(
              "2 there is no enough time: " + connectionTimeoutMs);
        }

        NiftyClientChannel channel = generateChannel(endPoint.getHostName(), endPoint.getPort(),
            leftTime);
        manager = new PyAsyncClientManager(channel, asyncMethodCallHelper);
        managers.add(manager);
        mapFromEndPointToThrift.put(endPoint, managers);
      }
    }

    return manager;
  }

  public void close() {
    mapFromEndPointToThrift.clear();
    if (niftyClient != null) {
      niftyClient.close();
    }
  }

  public GenericThriftClientFactory<T> withMaxConnectionsPerEndpoint(
      int maxConnectionsPerEndpoint) {
    return this;
  }

  public GenericThriftClientFactory<T> withDeadConnectionRestoreInterval(
      long connectionRestoreInterval) {
   
   
    return this;
  }

  public GenericThriftClientFactory<T> withMaxChannelPendingSizeMb(int maxChannelPendingSizeMb) {
    niftyClient.getNettyClientConfig().setMaxChannelPendingSizeMb(maxChannelPendingSizeMb);
    return this;
  }

  public GenericThriftClientFactory<T> useFramedTransport(boolean framedTransport) {
   
    return this;
  }

  public GenericThriftClientFactory<T> withDefaultSocketTimeout(int defaultSocketTimeoutMs) {
    this.defaultSocketTimeoutMs = defaultSocketTimeoutMs;
    return this;
  }

  public GenericThriftClientFactory<T> withDefaultConnectionTimeout(
      int defaultConnectionTimeoutMs) {
    this.defaultConnectionTimeoutMs = defaultConnectionTimeoutMs;
    return this;
  }

  public GenericThriftClientFactory<T> withSslEnabled() {
   
    return this;
  }

  public GenericThriftClientFactory<T> withNumberRetries(int retries) {
   
    return this;
  }

  public GenericThriftClientFactory<T> withStatsEnabled(boolean enabled) {
   
    return this;
  }

  public void setChannelCountToEndPoint(EndPoint endPoint, int channelCount) {
    if (mapFromEndPointToThrift.get(endPoint) == null) {
      mapFromEndPointToThrift
          .put(endPoint, Collections.synchronizedList(new ArrayList<PyAsyncClientManager>()));
    }

    if (mapFromEndPointToRoundRobin.get(endPoint) == null) {
      mapFromEndPointToRoundRobin.put(endPoint, new AtomicInteger());
    }

    if (mapEndPointToLock.get(endPoint) == null) {
      mapEndPointToLock.put(endPoint, new Object());
    }

    mapFromEndPointToChannelCount.put(endPoint, channelCount);
  }

  public int getCurrentChannelCount(EndPoint endPoint) {
    return mapFromEndPointToThrift.get(endPoint).size();
  }

  private NiftyClientChannel generateChannel(String hostName, int port, int timeoutMs)
      throws GenericThriftClientFactoryException {
    ListenableFuture<FramedClientChannel> future = null;
    NiftyClientChannel channel;
    try {
      InetSocketAddress address = new InetSocketAddress(hostName, port);
      FramedClientConnector framedClientConnector = new FramedClientConnector(address,
          TransDuplexProtocolFactory.fromSingleFactory(protocolFactory));
      future = niftyClient.connectAsync(framedClientConnector, timeoutMs, maxNetworkFrameSize);
      channel = future.get(timeoutMs, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      if (future != null) {
        future.cancel(true);
      }

      throw new GenericThriftClientFactoryException(e);
    }

    return channel;
  }

  private PyAsyncClientManager getManager(List<PyAsyncClientManager> managers, int index) {
    if (managers == null) {
      return null;
    }

    Object[] tmpManagers = managers.toArray();
    if (index < tmpManagers.length) {
      return (PyAsyncClientManager) tmpManagers[index];
    } else {
      return null;
    }
  }

  public GenericThriftClientFactory<T> setMaxNetworkFrameSize(int maxNetworkFrameSize) {
    this.maxNetworkFrameSize = maxNetworkFrameSize;
    return this;
  }

  public int getNetworkMaxFrameSize() {
    return maxNetworkFrameSize;
  }

  public void releaseSyncClient(T client) {
    if (!needCache) {
      return;
    }

    if (client == null) {
      return;
    }

    PyAsyncClientManager pyAsyncClientManager = mapClientToManager.get(client);
    if (pyAsyncClientManager == null) {
      logger.warn("the client={} not belong some manager={}", client, mapClientToManager.size());
    }

    if (!pyAsyncClientManager.recycle(client)) {
      mapClientToManager.remove(client);
    }
  }
}
