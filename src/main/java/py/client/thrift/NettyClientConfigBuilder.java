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

import static java.util.concurrent.Executors.newCachedThreadPool;

import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import org.jboss.netty.channel.socket.nio.NioSocketChannelConfig;
import org.jboss.netty.util.Timer;
import py.common.rpc.share.NettyConfigBuilderBase;
import py.common.rpc.share.NiftyTimer;

public class NettyClientConfigBuilder extends NettyConfigBuilderBase<NettyClientConfigBuilder> {
  public static final int MIN_DEFAULT_RESPONSE_THREAD_COUNT = Runtime.getRuntime()
      .availableProcessors();
  public static final int MAX_DEFAULT_RESPONSE_THREAD_COUNT =
      Runtime.getRuntime().availableProcessors() * 10;
  private final NioSocketChannelConfig socketChannelConfig = (NioSocketChannelConfig) Proxy
      .newProxyInstance(
          getClass().getClassLoader(), new Class<?>[]{NioSocketChannelConfig.class}, new Magic(""));
  private HostAndPort defaultSocksProxyAddress = null;
  private int minResponseThreadCount = MIN_DEFAULT_RESPONSE_THREAD_COUNT;
  private int maxResponseThreadCount = MAX_DEFAULT_RESPONSE_THREAD_COUNT;
  private int maxChannelPendingSizeMb = 200;

  private int maxPendingRequestCount = 2000;

  private int closeWaitTime = 10;

  @Inject
  public NettyClientConfigBuilder() {
    getSocketChannelConfig().setTcpNoDelay(true);
  }

  public static ExecutorService buildExecutor(String suffix) {
    return newCachedThreadPool(renamingDaemonThreadFactory(suffix + "-%s"));
  }

  private static ThreadFactory renamingDaemonThreadFactory(String nameFormat) {
    return new ThreadFactoryBuilder().setNameFormat(nameFormat).setDaemon(true).build();
  }

  public NioSocketChannelConfig getSocketChannelConfig() {
    return socketChannelConfig;
  }

  public NettyClientConfigBuilder setDefaultSocksProxyAddress(
      HostAndPort defaultSocksProxyAddress) {
    this.defaultSocksProxyAddress = defaultSocksProxyAddress;
    return this;
  }

  public void setMinResponseThreadCount(int responseThreadCount) {
    this.minResponseThreadCount = responseThreadCount;
  }

  public void setMaxChannelPendingSizeMb(int maxChannelPendingSizeMb) {
    this.maxChannelPendingSizeMb = maxChannelPendingSizeMb;
  }

  public void setMaxPendingReqeustCount(int maxPendingRequestCount) {
    this.maxPendingRequestCount = maxPendingRequestCount;
  }

  @SuppressWarnings("resource")
  public NettyClientConfig build() {
    Timer timer = getTimer();
    ExecutorService bossExecutor = getBossExecutor();
    int bossThreadCount = getBossThreadCount();
    ExecutorService workerExecutor = getWorkerExecutor();
    int workerThreadCount = getWorkerThreadCount();

    return new NettyClientConfig(getBootstrapOptions(), defaultSocksProxyAddress,
        timer != null ? timer
            : new NiftyTimer(getNiftyName()), bossExecutor != null ? bossExecutor
        : buildDefaultBossExecutor(), bossThreadCount, workerExecutor != null ? workerExecutor
        : buildDefaultWorkerExecutor(), workerThreadCount, maxChannelPendingSizeMb,
        maxPendingRequestCount).setMinResponseThreadCount(minResponseThreadCount)
        .setMaxResponseThreadCount(maxResponseThreadCount).setCloseWaitTime(closeWaitTime);
  }

  public NettyClientConfig buildWithBossExecutor() {
    int workerThreadCount = getWorkerThreadCount();
    int bossThreadCount = getBossThreadCount();

    return new NettyClientConfig(getBootstrapOptions(), defaultSocksProxyAddress, null, null,
        bossThreadCount,
        null, workerThreadCount, maxChannelPendingSizeMb, maxPendingRequestCount)
        .setMinResponseThreadCount(minResponseThreadCount).setMaxResponseThreadCount(
            maxResponseThreadCount).setCloseWaitTime(closeWaitTime);
  }

  private ExecutorService buildDefaultBossExecutor() {
    return newCachedThreadPool(renamingDaemonThreadFactory(threadNamePattern("-boss-%s")));
  }

  private ExecutorService buildDefaultWorkerExecutor() {
    return newCachedThreadPool(renamingDaemonThreadFactory(threadNamePattern("-worker-%s")));
  }

  private String threadNamePattern(String suffix) {
    String niftyName = getNiftyName();
    return "py-client" + (Strings.isNullOrEmpty(niftyName) ? "" : "-" + niftyName) + suffix;
  }

  public int getMaxResponseThreadCount() {
    return maxResponseThreadCount;
  }

  public void setMaxResponseThreadCount(int maxResponseThreadCount) {
    this.maxResponseThreadCount = maxResponseThreadCount;
  }

  public void setCloseWaitTime(int closeWaitTime) {
    this.closeWaitTime = closeWaitTime;
  }
}
