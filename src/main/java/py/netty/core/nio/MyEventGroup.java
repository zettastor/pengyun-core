/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package py.netty.core.nio;

import io.netty.channel.DefaultSelectStrategyFactory;
import io.netty.channel.EventLoop;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.SelectStrategyFactory;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorChooserFactory;
import io.netty.util.concurrent.RejectedExecutionHandler;
import io.netty.util.concurrent.RejectedExecutionHandlers;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public class MyEventGroup extends MultithreadEventLoopGroup {
  public MyEventGroup() {
    this(0);
  }

  public MyEventGroup(int threadsN) {
    this(threadsN, (Executor) null);
  }

  public MyEventGroup(int threadsN, ThreadFactory threadFactory) {
    this(threadsN, threadFactory, SelectorProvider.provider());
  }

  public MyEventGroup(int threadsN, Executor executor) {
    this(threadsN, executor, SelectorProvider.provider());
  }

  public MyEventGroup(
      int threadsN, ThreadFactory threadFactory, final SelectorProvider selectorProvider) {
    this(threadsN, threadFactory, selectorProvider, DefaultSelectStrategyFactory.INSTANCE);
  }

  public MyEventGroup(int threadsN, ThreadFactory threadFactory,
      final SelectorProvider selectorProvider, final SelectStrategyFactory selectStrategyFactory) {
    super(threadsN, threadFactory, selectorProvider, selectStrategyFactory,
        RejectedExecutionHandlers.reject());
  }

  public MyEventGroup(
      int threadsN, Executor executor, final SelectorProvider selectorProvider) {
    this(threadsN, executor, selectorProvider, DefaultSelectStrategyFactory.INSTANCE);
  }

  public MyEventGroup(int threadsN, Executor executor, final SelectorProvider selectorProvider,
      final SelectStrategyFactory selectStrategyFactory) {
    super(threadsN, executor, selectorProvider, selectStrategyFactory,
        RejectedExecutionHandlers.reject());
  }

  public MyEventGroup(int threadsN, Executor executor, EventExecutorChooserFactory chooserFactory,
      final SelectorProvider selectorProvider,
      final SelectStrategyFactory selectStrategyFactory) {
    super(threadsN, executor, chooserFactory, selectorProvider, selectStrategyFactory,
        RejectedExecutionHandlers.reject());
  }

  public MyEventGroup(int threadsN, Executor executor, EventExecutorChooserFactory chooserFactory,
      final SelectorProvider selectorProvider,
      final SelectStrategyFactory selectStrategyFactory,
      final RejectedExecutionHandler rejectedExecutionHandler) {
    super(threadsN, executor, chooserFactory, selectorProvider, selectStrategyFactory,
        rejectedExecutionHandler);
  }

  public void setIoRatio(int ioRatio) {
    for (EventExecutor e : this) {
      ((MyEventLoop) e).setIoRatio(ioRatio);
    }
  }

  public void rebuildSelectors() {
    for (EventExecutor e : this) {
      ((MyEventLoop) e).rebuildSelector();
    }
  }

  @Override
  protected EventLoop newChild(Executor executor, Object... args) throws Exception {
    return new MyEventLoop(this, executor, (SelectorProvider) args[0],
        ((SelectStrategyFactory) args[1]).newSelectStrategy(), (RejectedExecutionHandler) args[2]);
  }
}
