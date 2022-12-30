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

import com.google.common.net.HostAndPort;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.jboss.netty.util.Timer;

public class NettyClientConfig {
  private final Map<String, Object> bootstrapOptions;
  private final HostAndPort defaultSocksProxyAddress;
  private final int bossThreadCount;
  private final ExecutorService workerExecutor;
  private Timer timer;
  private ExecutorService bossExecutor;
  private int workerThreadCount;
  private int minResponseThreadCount;
  private int maxResponseThreadCount;
  private int maxChannelPendingSizeMb;
  private int maxPendingRequestCount;
  private int closeWaitTime;

  public NettyClientConfig(Map<String, Object> bootstrapOptions,
      HostAndPort defaultSocksProxyAddress, Timer timer,
      ExecutorService bossExecutor, int bossThreadCount, ExecutorService workerExecutor,
      int workerThreadCount, int maxChannelPendingSizeMb, int maxPendingRequestCount) {
    this.bootstrapOptions = bootstrapOptions;
    this.defaultSocksProxyAddress = defaultSocksProxyAddress;
    this.timer = timer;
    this.bossExecutor = bossExecutor;
    this.bossThreadCount = bossThreadCount;
    this.workerExecutor = workerExecutor;
    this.workerThreadCount = workerThreadCount;
    this.maxChannelPendingSizeMb = maxChannelPendingSizeMb;
    this.maxPendingRequestCount = maxPendingRequestCount;
  }

  public static NettyClientConfigBuilder newBuilder() {
    return new NettyClientConfigBuilder();
  }

  public Map<String, Object> getBootstrapOptions() {
    return bootstrapOptions;
  }

  public ExecutorService getBossExecutor() {
    return bossExecutor;
  }

  public void setBossExecutor(ExecutorService bossExecutor) {
    this.bossExecutor = bossExecutor;
  }

  public int getMaxChannelPendingSizeMb() {
    return maxChannelPendingSizeMb;
  }

  public void setMaxChannelPendingSizeMb(int maxChannelPendingSizeMb) {
    this.maxChannelPendingSizeMb = maxChannelPendingSizeMb;
  }

  public int getBossThreadCount() {
    return bossThreadCount;
  }

  public HostAndPort getDefaultSocksProxyAddress() {
    return defaultSocksProxyAddress;
  }

  public Timer getTimer() {
    return timer;
  }

  public void setTimer(Timer timer) {
    this.timer = timer;
  }

  public ExecutorService getWorkerExecutor() {
    return workerExecutor;
  }

  public int getWorkerThreadCount() {
    return workerThreadCount;
  }

  public NettyClientConfig setWorkerThreadCount(int workerThreadCount) {
    this.workerThreadCount = workerThreadCount;
    return this;
  }

  public int getMinResponseThreadCount() {
    return minResponseThreadCount;
  }

  public NettyClientConfig setMinResponseThreadCount(int responseThreadCount) {
    this.minResponseThreadCount = responseThreadCount;
    return this;
  }

  public int getMaxResponseThreadCount() {
    return maxResponseThreadCount;
  }

  public NettyClientConfig setMaxResponseThreadCount(int maxResponseThreadCount) {
    this.maxResponseThreadCount = maxResponseThreadCount;
    return this;
  }

  public int getMaxPendingRequestCount() {
    return maxPendingRequestCount;
  }

  public int getCloseWaitTime() {
    return closeWaitTime;
  }

  public NettyClientConfig setCloseWaitTime(int closeWaitTime) {
    this.closeWaitTime = closeWaitTime;
    return this;
  }
}
