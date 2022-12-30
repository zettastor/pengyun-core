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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.RejectedExecutionHandler;
import io.netty.util.concurrent.RejectedExecutionHandlers;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.UnstableApi;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.netty.core.PyTimeToFlush;

public abstract class SingleThreadEventLoop extends SingleThreadEventExecutor implements EventLoop {
  protected static final int DEFAULT_MAX_PENDING_TASKS = Math
      .max(16, SystemPropertyUtil.getInt("io.netty.eventLoop.maxPendingTasks", Integer.MAX_VALUE));
  private static final Logger logger = LoggerFactory.getLogger(SingleThreadEventLoop.class);
  private final Queue<Runnable> tailTasks;

  private PyTimeToFlush ttf;

  private Map<ChannelId, ChannelHandlerContext> channelHandlerContextMap;

  protected SingleThreadEventLoop(EventLoopGroup parent, ThreadFactory threadFactory,
      boolean addTaskWakesUp) {
    this(parent, threadFactory, addTaskWakesUp, DEFAULT_MAX_PENDING_TASKS,
        RejectedExecutionHandlers.reject());
  }

  protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor,
      boolean addTaskWakesUp) {
    this(parent, executor, addTaskWakesUp, DEFAULT_MAX_PENDING_TASKS,
        RejectedExecutionHandlers.reject());
  }

  protected SingleThreadEventLoop(EventLoopGroup parent, ThreadFactory threadFactory,
      boolean addTaskWakesUp,
      int maxPendingTasks, RejectedExecutionHandler rejectedExecutionHandler) {
    super(parent, threadFactory, addTaskWakesUp, maxPendingTasks, rejectedExecutionHandler);
    tailTasks = newTaskQueue(maxPendingTasks);
  }

  protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor, boolean addTaskWakesUp,
      int maxPendingTasks, RejectedExecutionHandler rejectedExecutionHandler) {
    super(parent, executor, addTaskWakesUp, maxPendingTasks, rejectedExecutionHandler);
    tailTasks = newTaskQueue(maxPendingTasks);
    this.channelHandlerContextMap = new HashMap<>();
  }

  @Override
  public EventLoopGroup parent() {
    return (EventLoopGroup) super.parent();
  }

  @Override
  public EventLoop next() {
    return (EventLoop) super.next();
  }

  @Override
  public ChannelFuture register(Channel channel) {
    return register(new DefaultChannelPromise(channel, this));
  }

  @Override
  public ChannelFuture register(final ChannelPromise promise) {
    ObjectUtil.checkNotNull(promise, "promise");
    promise.channel().unsafe().register(this, promise);
    return promise;
  }

  @Deprecated
  @Override
  public ChannelFuture register(final Channel channel, final ChannelPromise promise) {
    if (channel == null) {
      throw new NullPointerException("channel");
    }
    if (promise == null) {
      throw new NullPointerException("promise");
    }

    channel.unsafe().register(this, promise);
    return promise;
  }

  @UnstableApi
  public final void executeAfterEventLoopIteration(Runnable task) {
    ObjectUtil.checkNotNull(task, "task");
    if (isShutdown()) {
      reject();
    }

    if (!tailTasks.offer(task)) {
      reject(task);
    }

    if (wakesUpForTask(task)) {
      wakeup(inEventLoop());
    }
  }

  @UnstableApi
  final boolean removeAfterEventLoopIterationTask(Runnable task) {
    return tailTasks.remove(ObjectUtil.checkNotNull(task, "task"));
  }

  @Override
  protected boolean wakesUpForTask(Runnable task) {
    return !(task instanceof NonWakeupRunnable);
  }

  @Override
  protected void afterRunningAllTasks() {
    if (ttf != null) {
      ttf.call();
    }
    runAllTasksFrom(tailTasks);
  }

  @Override
  protected boolean hasTasks() {
    return super.hasTasks() || !tailTasks.isEmpty();
  }

  @Override
  public int pendingTasks() {
    return super.pendingTasks() + tailTasks.size();
  }

  public void setTimeToFlushCallback(PyTimeToFlush ttf) {
    this.ttf = ttf;
  }

  public void addChannelToFlush(ChannelHandlerContext channelHandlerContext) {
    this.channelHandlerContextMap.put(channelHandlerContext.channel().id(), channelHandlerContext);
  }

  public void flushAllChannels() {
    logger.debug("flush map size:{}", this.channelHandlerContextMap.size());
    if (!this.channelHandlerContextMap.isEmpty()) {
      for (ChannelHandlerContext context : this.channelHandlerContextMap.values()) {
        context.flush();
      }
      this.channelHandlerContextMap.clear();
    }
  }

  interface NonWakeupRunnable extends Runnable {
  }

}
