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

package py.netty.client;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.NamedThreadFactory;
import py.common.struct.EndPoint;
import py.connection.pool.BootstrapBuilder;
import py.connection.pool.PyConnection;
import py.connection.pool.PyConnectionPoolImpl;
import py.netty.core.IoEventThreadsMode;
import py.netty.core.IoEventUtils;
import py.netty.core.Protocol;
import py.netty.core.ProtocolFactory;
import py.netty.core.TransferenceConfiguration;
import py.netty.core.twothreads.TtEventGroup;
import py.netty.memory.PooledByteBufAllocatorWrapper;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GenericAsyncClientFactory<T> {
  private static final Logger logger = LoggerFactory.getLogger(GenericAsyncClientFactory.class);
  private final TransferenceConfiguration cfg;
  private Class<T> service;
  private ProtocolFactory protocolFactory;
  private HashedWheelTimer hashedWheelTimer;
  private PyConnectionPoolImpl connectionPool;
  private int connectionPoolSize;

  private ThreadLocal<ThreadLocalValue> threadLocal = new ThreadLocal();
  private ByteBufAllocator allocator = PooledByteBufAllocatorWrapper.INSTANCE;
  private BootstrapBuilder bootstrapBuilder;

  private TtEventGroup ioEventGroup;

  private ExecutorService executorForProcessingResponses;

  public GenericAsyncClientFactory(Class<T> serviceInterface, ProtocolFactory protocolFactory,
      TransferenceConfiguration cfg) {
    this(serviceInterface, protocolFactory, cfg, "");
  }

  public GenericAsyncClientFactory(Class<T> serviceInterface, ProtocolFactory protocolFactory,
      TransferenceConfiguration cfg, String prefix) {
    Validate.isTrue(serviceInterface.isInterface());
    this.service = (Class<T>) findImplementationClass(serviceInterface);
    this.cfg = cfg;
    this.protocolFactory = protocolFactory;

    int ioEventGroupThreads = IoEventUtils
        .calculateThreads((IoEventThreadsMode) cfg
                .valueOf(TransferenceClientOption.CLIENT_IO_EVENT_GROUP_THREADS_MODE),
            (Float) cfg.valueOf(TransferenceClientOption.CLIENT_IO_EVENT_GROUP_THREADS_PARAMETER));
    ioEventGroup = new TtEventGroup(ioEventGroupThreads,
        new DefaultThreadFactory("netty-client-io", false, Thread.NORM_PRIORITY));

    int numberOfThreadsToProcessResponses = IoEventUtils
        .calculateThreads((IoEventThreadsMode) cfg
                .valueOf(TransferenceClientOption.CLIENT_IO_EVENT_HANDLE_THREADS_MODE),
            (Float) cfg.valueOf(TransferenceClientOption.CLIENT_IO_EVENT_HANDLE_THREADS_PARAMETER));
    executorForProcessingResponses = new ThreadPoolExecutor(numberOfThreadsToProcessResponses,
        numberOfThreadsToProcessResponses, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
        new NamedThreadFactory("netty-client-response-processor-" + prefix));
    validateService();
  }

  public static TransferenceConfiguration defaultConfiguration() {
    TransferenceConfiguration clientConfiguration = TransferenceConfiguration
        .defaultConfiguration();
    clientConfiguration.option(TransferenceClientOption.IO_CONNECTION_TIMEOUT_MS, 3000);
    clientConfiguration.option(TransferenceClientOption.IO_TIMEOUT_MS, 10000);
    clientConfiguration.option(TransferenceClientOption.CONNECTION_COUNT_PER_ENDPOINT, 1);
    clientConfiguration
        .option(TransferenceClientOption.MAX_LAST_TIME_FOR_CONNECTION_MS, 60 * 1000 * 15);
    return clientConfiguration;
  }

  private static <T> Class<? extends T> findImplementationClass(final Class<T> serviceInterface) {
    try {
      return (Class<? extends T>) Iterables
          .find(ImmutableList.copyOf(serviceInterface.getEnclosingClass().getClasses()),
              new Predicate<Class<?>>() {
                @Override
                public boolean apply(Class<?> inner) {
                  logger.info("inner: {}", inner);
                  return !serviceInterface.equals(inner) && serviceInterface
                      .isAssignableFrom(inner);
                }
              });
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Could not find a sibling enclosed implementation of " + "service interface: "
              + serviceInterface);
    }
  }

  public void validateService() {
    try {
      service.getConstructor(PyConnection.class, Protocol.class);
    } catch (Exception e) {
      logger.error("caught an exception", e);
      throw new RuntimeException();
    }
  }

  public void init() {
    this.hashedWheelTimer = new HashedWheelTimer(
        new ThreadFactoryBuilder().setNameFormat("nett4-wheel-timer-%s").build(), 100,
        TimeUnit.MILLISECONDS,
        512);
    final int ioTimeout = (int) cfg.valueOf(TransferenceClientOption.IO_TIMEOUT_MS);

    protocolFactory.setByteBufAllocator(allocator);

    if (bootstrapBuilder == null) {
      bootstrapBuilder = new BootstrapBuilderImpl(executorForProcessingResponses);
    }

    bootstrapBuilder.setAllocator(allocator);
    bootstrapBuilder.setCfg(cfg);
    bootstrapBuilder.setHashedWheelTimer(hashedWheelTimer);
    bootstrapBuilder.setProtocolFactory(protocolFactory);
    bootstrapBuilder.setIoEventGroup(ioEventGroup);

    connectionPoolSize = (int) cfg.valueOf(TransferenceClientOption.CONNECTION_COUNT_PER_ENDPOINT);
    connectionPoolSize = connectionPoolSize <= 0 ? 1 : connectionPoolSize;

    int maxLastTimeForConnectionMs = (int) cfg
        .valueOf(TransferenceClientOption.MAX_LAST_TIME_FOR_CONNECTION_MS);
    logger.warn(
        "connection pool size is {}\n maxLastTimeForConnectionMS {}\n",
        connectionPoolSize, maxLastTimeForConnectionMs);

    this.connectionPool = new PyConnectionPoolImpl(connectionPoolSize, maxLastTimeForConnectionMs,
        bootstrapBuilder);

    logger.warn(
        "connection pool size: {}. maxLastTimeForConnectionMs={}, ioTimeout={} UDPListenerPort={}",
        connectionPoolSize, maxLastTimeForConnectionMs, ioTimeout);
  }

  public T generate(EndPoint endPoint) {
    ThreadLocalValue value = threadLocal.get();
    if (value == null) {
      value = new ThreadLocalValue();
      threadLocal.set(value);
    }

    T[] existing = value.cacheClients.get(endPoint);
    AtomicInteger sequence = null;
    if (existing == null) {
      existing = (T[]) new Object[connectionPoolSize];
      sequence = new AtomicInteger(0);
      value.cacheClients.put(endPoint, existing);
      value.mapEndPointToSequence.put(endPoint, sequence);
    } else {
      sequence = value.mapEndPointToSequence.get(endPoint);
    }

    int currentIndex = Math.abs(sequence.getAndIncrement()) % connectionPoolSize;
    if (existing[currentIndex] != null) {
      return existing[currentIndex];
    }

    try {
      PyConnection connection = connectionPool.get(endPoint);
      Object[] intArgs = new Object[]{connection, protocolFactory.getProtocol()};
      existing[currentIndex] = (T) service.getConstructor(PyConnection.class, Protocol.class)
          .newInstance(intArgs);
      return existing[currentIndex];
    } catch (Exception e) {
      logger.error("caught an exception", e);
      throw new RuntimeException();
    }
  }

  public PyConnectionPoolImpl getConnectionPool() {
    return connectionPool;
  }

  public void close() {
    connectionPool.close();

    Set<Timeout> timeouts = hashedWheelTimer.stop();
    logger.warn("stop the timer, but there is timeout: {}", timeouts.size());
    for (Timeout timeout : timeouts) {
      timeout.cancel();
    }

    try {
      if (ioEventGroup != null) {
        ioEventGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS);
        ioEventGroup.awaitTermination(6, TimeUnit.SECONDS);
      }
    } catch (InterruptedException e) {
      logger.warn("Caught an exception", e);
    }

    try {
      if (executorForProcessingResponses != null) {
        executorForProcessingResponses.shutdown();
        executorForProcessingResponses.awaitTermination(6, TimeUnit.SECONDS);
      }
    } catch (InterruptedException e) {
      logger.warn("Caught an exception when close client processors", e);
    }
  }

  public TransferenceConfiguration getCfg() {
    return cfg;
  }

  public ByteBufAllocator getAllocator() {
    return allocator;
  }

  public void setAllocator(ByteBufAllocator allocator) {
    this.allocator = allocator;
  }

  public void setBootstrapBuilder(BootstrapBuilder bootstrapBuilder) {
    this.bootstrapBuilder = bootstrapBuilder;
  }

  private class ThreadLocalValue {
    final Map<EndPoint, T[]> cacheClients;
    final Map<EndPoint, AtomicInteger> mapEndPointToSequence;

    public ThreadLocalValue() {
      this.cacheClients = new HashMap<>();
      this.mapEndPointToSequence = new HashMap<>();
    }
  }
}
