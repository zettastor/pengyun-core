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

package py.netty.core.twothreads;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SelectStrategy;
import io.netty.util.concurrent.AbstractScheduledEventExecutor;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.ReflectionUtil;
import io.netty.util.internal.SystemPropertyUtil;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TtEventLoop extends AbstractScheduledEventExecutor implements EventLoop {
  protected static final int DEFAULT_MAX_PENDING_TASKS = Math
      .max(16, SystemPropertyUtil.getInt("io.netty.eventLoop.maxPendingTasks", Integer.MAX_VALUE));
  private static final Logger logger = LoggerFactory.getLogger(TtEventLoop.class);
  private static final boolean DISABLE_KEYSET_OPTIMIZATION = SystemPropertyUtil
      .getBoolean("io.netty.noKeySetOptimization", false);

  private static final int CLEANUP_INTERVAL = 256;

  private static final int MIN_PREMATURE_SELECTOR_RETURNS = 3;
  private static final int SELECTOR_AUTO_REBUILD_THRESHOLD;
  private static final NetworkTask SHUTDOWN_TASK = new NetworkTask() {
    @Override
    public int compareTo(Delayed o) {
      return 0;
    }

    @Override
    public long getDelay(TimeUnit unit) {
      return 0;
    }

    @Override
    public void run() {
    }
  };
  private static AtomicInteger threadSeqId = new AtomicInteger(0);

  static {
    final String key = "sun.nio.ch.bugLevel";
    final String buglevel = SystemPropertyUtil.get(key);
    if (buglevel == null) {
      try {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
          @Override
          public Void run() {
            System.setProperty(key, "");
            return null;
          }
        });
      } catch (final SecurityException e) {
        logger.debug("Unable to get/set System Property: " + key, e);
      }
    }

    int selectorAutoRebuildThreshold = SystemPropertyUtil
        .getInt("io.netty.selectorAutoRebuildThreshold", 512);
    if (selectorAutoRebuildThreshold < MIN_PREMATURE_SELECTOR_RETURNS) {
      selectorAutoRebuildThreshold = 0;
    }

    SELECTOR_AUTO_REBUILD_THRESHOLD = selectorAutoRebuildThreshold;

    if (logger.isDebugEnabled()) {
      logger.debug("-Dio.netty.noKeySetOptimization: {}", DISABLE_KEYSET_OPTIMIZATION);
      logger.debug("-Dio.netty.selectorAutoRebuildThreshold: {}", SELECTOR_AUTO_REBUILD_THRESHOLD);
    }
  }

  private final Collection<EventExecutor> selfCollection = Collections
      .<EventExecutor>singleton(this);
  private final Promise<?> terminationFuture = new DefaultPromise<Void>(
      GlobalEventExecutor.INSTANCE);

  private final int seqId;
  private final SelectorProvider provider;
  private final SynchronizerBetweenEventThreadAndReaderThread synchronizer;
  private final Thread scheduledTaskPollerThread;
  private final ScheduleTaskPoller scheduledTaskPollerRunnable;
  private final Reader readerThreadRunnable;
  private final Thread readerThread;

  private Selector selector;
  private Selector unwrappedSelector;
  private SelectedSelectionKeySet selectedKeys;
  private int cancelledKeys;
  private boolean needsToSelectAgain;
  private EventLoopGroup loopGroup;
  private NetworkTaskEngine taskEngine;

  TtEventLoop(TtEventGroup parent, SelectorProvider selectorProvider, SelectStrategy strategy) {
    super(parent);

    if (selectorProvider == null) {
      throw new NullPointerException("selectorProvider");
    }
    if (strategy == null) {
      throw new NullPointerException("selectStrategy");
    }
    loopGroup = parent;
    provider = selectorProvider;
    seqId = threadSeqId.incrementAndGet();

    final SelectorTuple selectorTupleForRead = openSelector();
    selector = selectorTupleForRead.selector;
    unwrappedSelector = selectorTupleForRead.unwrappedSelector;

    try {
      synchronizer = new SynchronizerBetweenEventThreadAndReaderThread(seqId, unwrappedSelector);
    } catch (IOException e) {
      logger.warn("can't construct a channel for threads in EventLoop to communicate", e);
      throw new RuntimeException("TTEventLoop");
    }

    taskEngine = new NetworkTaskEngineUsingBlockingQueue<Runnable>(new Consumer<Runnable>() {
      @Override
      public void accept(Runnable task) {
        try {
          task.run();
        } catch (Throwable t) {
          logger.warn("The execution of the task {} ", task, t);
        }
      }
    }, "tt-event-task-engine-" + seqId) {
      private Set<Channel> channelsHavingPendingWrites = new HashSet<>(16);

      @Override
      protected void enqueueShutdownTask() {
        enqueue(SHUTDOWN_TASK);
      }

      @Override
      protected boolean isShutdownTask(Runnable runnable) {
        return runnable == SHUTDOWN_TASK;
      }

      @Override
      protected BlockingQueue<Runnable> newTaskQueue() {
        return new LinkedBlockingQueue<>();
      }

      @Override
      protected void safeExecute(Runnable task) {
        if (task instanceof AbstractChannelHandlerContext.WriteTask) {
          Channel channel = ((AbstractChannelHandlerContext.WriteTask) task).getCtx().channel();
          Validate.notNull(channel);
          channelsHavingPendingWrites.add(channel);
        }
        super.safeExecute(task);
      }

      @Override
      public void afterRunningAllTasks(int runTasks) {
        for (Channel channel : channelsHavingPendingWrites) {
          channel.unsafe().flush();
        }

        channelsHavingPendingWrites.clear();
      }
    };

    taskEngine.start();

    scheduledTaskPollerRunnable = new ScheduleTaskPoller();
    scheduledTaskPollerThread = new Thread(scheduledTaskPollerRunnable);

    scheduledTaskPollerThread.setName("tt-event-scheduled-task-puller-" + seqId);
    scheduledTaskPollerThread.start();

    readerThreadRunnable = new Reader();
    readerThread = new Thread(readerThreadRunnable);
    readerThread.setName("tt-event-io-reader-" + seqId);
    readerThread.start();

    if (logger.isDebugEnabled()) {
      logger.debug("-Dio.netty.eventLoop.maxPendingTasks: {}", DEFAULT_MAX_PENDING_TASKS);
      logger.debug("-D:io.netty.noKeySetOptimization {}", DISABLE_KEYSET_OPTIMIZATION);
    }
  }

  private static void handleLoopException(Throwable t) {
    logger.warn("Unexpected exception in the selector loop.", t);

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // do nothing
    }
  }

  private SelectorTuple openSelector() {
    final Selector unwrappedSelector;
    try {
      unwrappedSelector = provider.openSelector();
    } catch (IOException e) {
      throw new ChannelException("failed to open a new selector", e);
    }

    if (DISABLE_KEYSET_OPTIMIZATION) {
      logger.warn("DISABLE_KEYSET_OPTIMIZATION is set to true");
      return new SelectorTuple(unwrappedSelector);
    }

    final SelectedSelectionKeySet selectedKeySet = new SelectedSelectionKeySet();

    Object maybeSelectorImplClass = AccessController.doPrivileged(new PrivilegedAction<Object>() {
      @Override
      public Object run() {
        try {
          return Class
              .forName("sun.nio.ch.SelectorImpl", false, PlatformDependent.getSystemClassLoader());
        } catch (Throwable cause) {
          return cause;
        }
      }
    });

    if (!(maybeSelectorImplClass instanceof Class)

        || !((Class<?>) maybeSelectorImplClass).isAssignableFrom(unwrappedSelector.getClass())) {
      if (maybeSelectorImplClass instanceof Throwable) {
        Throwable t = (Throwable) maybeSelectorImplClass;
        logger.trace("failed to instrument a special java.util.Set into: {}", unwrappedSelector, t);
      }
      return new SelectorTuple(unwrappedSelector);
    }

    final Class<?> selectorImplClass = (Class<?>) maybeSelectorImplClass;

    Object maybeException = AccessController.doPrivileged(new PrivilegedAction<Object>() {
      @Override
      public Object run() {
        try {
          Field selectedKeysField = selectorImplClass.getDeclaredField("selectedKeys");
          Field publicSelectedKeysField = selectorImplClass.getDeclaredField("publicSelectedKeys");

          Throwable cause = ReflectionUtil.trySetAccessible(selectedKeysField, true);

          if (cause != null) {
            return cause;
          }
          cause = ReflectionUtil.trySetAccessible(publicSelectedKeysField, true);
          if (cause != null) {
            return cause;
          }

          selectedKeysField.set(unwrappedSelector, selectedKeySet);
          publicSelectedKeysField.set(unwrappedSelector, selectedKeySet);
          return null;
        } catch (NoSuchFieldException e) {
          return e;
        } catch (IllegalAccessException e) {
          return e;
        }
      }
    });

    if (maybeException instanceof Exception) {
      Exception e = (Exception) maybeException;
      logger.debug("failed to instrument a special java.util.Set into: {}", unwrappedSelector, e);
      return new SelectorTuple(unwrappedSelector);
    }
    selectedKeys = selectedKeySet;
    logger.debug("instrumented a special java.util.Set into: {}", unwrappedSelector);
    return new SelectorTuple(unwrappedSelector,
        new SelectedSelectionKeySetSelector(unwrappedSelector, selectedKeySet), selectedKeySet);
  }

  public SelectorProvider selectorProvider() {
    return provider;
  }

  @Override
  public EventLoopGroup parent() {
    return loopGroup;

  }

  @Override
  public boolean inEventLoop() {
    return inEventLoop(Thread.currentThread());
  }

  @Override
  public boolean inEventLoop(Thread thread) {
    return taskEngine.inEventLoop(thread) || thread == scheduledTaskPollerThread
        || thread == readerThread;
  }

  @Override
  public boolean isShuttingDown() {
    return taskEngine.isShuttingDown();
  }

  @Override
  public Future<?> shutdownGracefully() {
    return shutdownGracefully(1, 1, TimeUnit.SECONDS);
  }

  @Override
  public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
    taskEngine.shutdownGracefully(quietPeriod, timeout, unit);
    scheduledTaskPollerRunnable.stop = true;
    readerThreadRunnable.stop = true;
    scheduledTaskPollerThread.interrupt();
    readerThread.interrupt();

    return null;
  }

  @Override
  public Future<?> terminationFuture() {
    return terminationFuture;
  }

  @Override
  public void shutdown() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Runnable> shutdownNow() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isShutdown() {
    return taskEngine.isTerminated();
  }

  @Override
  public boolean isTerminated() {
    return taskEngine.isTerminated();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    long startTime = System.currentTimeMillis();
    taskEngine.awaitTermination(timeout, unit);

    long endTime = System.currentTimeMillis();
    long waitDuration = endTime - startTime;

    long timeToWaitInMs = unit.toMillis(timeout) - waitDuration;
    long timeWaitUnitMs = 1000;

    while (timeToWaitInMs > 0) {
      if (!(scheduledTaskPollerThread.isAlive() || readerThread.isAlive())) {
        logger.warn("All threads related to this eventLoop ({}) have been shutdown", seqId);
        break;
      } else {
        try {
          Thread.sleep(timeWaitUnitMs);
          logger.warn("waiting for reader and scheduling thread to be shutdown");
        } catch (InterruptedException e) {
          logger.error("interrupted", e);
        } finally {
          timeToWaitInMs -= timeWaitUnitMs;
        }
      }
    }
    return true;
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

  @Override
  public ChannelFuture register(Channel channel, ChannelPromise promise) {
    ObjectUtil.checkNotNull(promise, "promise");
    ObjectUtil.checkNotNull(channel, "channel");
    channel.unsafe().register(this, promise);
    return promise;
  }

  @Override
  public void execute(Runnable task) {
    if (task == null) {
      throw new NullPointerException("task");
    }

    taskEngine.submit(task);
  }

  public Selector unwrappedSelector() {
    return unwrappedSelector;
  }

  public int selectNow() throws IOException {
    return selector.selectNow();
  }

  private void debugKeys() {
    int i = 0;
    for (SelectionKey key : selector.keys()) {
      if (key.isValid()) {
        logger.debug("{}: interestOps {}, channel: {}", i++, key.interestOps(), key.channel());
      }
    }
  }

  private void selectAgain() {
    needsToSelectAgain = false;
    try {
      selector.selectNow();
    } catch (Throwable t) {
      logger.warn("Failed to update SelectionKeys.", t);
    }
  }

  public void rebuildSelector() {
    if (!inEventLoop()) {
      execute(new Runnable() {
        @Override
        public void run() {
          rebuildSelector0();
        }
      });
      return;
    }
    rebuildSelector0();
  }

  private void rebuildSelector0() {
    final Selector oldSelector = selector;
    final SelectorTuple newSelectorTuple;

    if (oldSelector == null) {
      return;
    }

    try {
      newSelectorTuple = openSelector();
    } catch (Exception e) {
      logger.warn("Failed to create a new Selector.", e);
      return;
    }

    int nchannels = 0;
    for (SelectionKey key : oldSelector.keys()) {
      Object a = key.attachment();
      try {
        if (!key.isValid() || key.channel().keyFor(newSelectorTuple.unwrappedSelector) != null) {
          continue;
        }

        int interestOps = key.interestOps();
        key.cancel();
        if (a instanceof AbstractNioChannel) {
          SelectionKey newKey = key.channel()
              .register(newSelectorTuple.unwrappedSelector, interestOps, a);

          ((AbstractNioChannel) a).selectionKey = newKey;
        } else {
          Validate.isTrue(false, "wrong channel type " + a.getClass());
        }

        nchannels++;
      } catch (Exception e) {
        logger.warn("Failed to re-register a Channel to the new Selector.", e);
        if (a instanceof AbstractNioChannel) {
          AbstractNioChannel ch = (AbstractNioChannel) a;
          ch.unsafe().close(ch.unsafe().voidPromise());
        } else {
          Validate.isTrue(false, "wrong channel type " + a.getClass());
        }
      }
    }

    selector = newSelectorTuple.selector;
    unwrappedSelector = newSelectorTuple.unwrappedSelector;
    synchronizer.register(unwrappedSelector);

    try {
      oldSelector.close();
    } catch (Throwable t) {
      if (logger.isWarnEnabled()) {
        logger.warn("Failed to close the old Selector.", t);
      }
    }

    logger.info("Migrated " + nchannels + " channel(s) to the new Selector.");
  }

  private void processSelectedKeysOptimized() {
    for (int i = 0; i < selectedKeys.size; ++i) {
      final SelectionKey k = selectedKeys.keys[i];

      selectedKeys.keys[i] = null;

      final Object a = k.attachment();

      if (a instanceof AbstractNioChannel) {
        processSelectedKey(k, (AbstractNioChannel) a);
      } else {
        Validate.isTrue(false, "no way we can be here");
      }

      if (needsToSelectAgain) {
        selectedKeys.reset(i + 1);

        i = -1;
        selectAgain();
      }
    }
  }

  private void processSelectedKey(SelectionKey k, AbstractNioChannel ch) {
    final AbstractNioChannel.NioUnsafe unsafe = ch.unsafe();
    if (!k.isValid()) {
      logger.warn("invalid key {}", k);
      final EventLoop eventLoop;
      try {
        eventLoop = ch.eventLoop();
      } catch (Throwable ignored) {
        return;
      }

      if (eventLoop != this || eventLoop == null) {
        return;
      }

      unsafe.close(unsafe.voidPromise());
      return;
    }

    try {
      int readyOps = k.readyOps();

      if ((readyOps & SelectionKey.OP_CONNECT) != 0) {
        int ops = k.interestOps();

        ops &= ~SelectionKey.OP_CONNECT;
        k.interestOps(ops);

        unsafe.finishConnect();
      }

      Validate.isTrue((readyOps & SelectionKey.OP_WRITE) == 0);

      if ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0 || readyOps == 0) {
        unsafe.read();
      }
    } catch (CancelledKeyException ignored) {
      unsafe.close(unsafe.voidPromise());
    }
  }

  private void processSelectedKeysPlain(Set<SelectionKey> selectedKeys) {
    if (selectedKeys.isEmpty()) {
      return;
    }

    Iterator<SelectionKey> i = selectedKeys.iterator();
    for (; ; ) {
      final SelectionKey k = i.next();
      final Object a = k.attachment();
      i.remove();

      if (a instanceof AbstractNioChannel) {
        processSelectedKey(k, (AbstractNioChannel) a);
      } else {
        Validate.isTrue(false, "no way here");
      }

      if (!i.hasNext()) {
        break;
      }

      if (needsToSelectAgain) {
        selectAgain();
        selectedKeys = selector.selectedKeys();

        if (selectedKeys.isEmpty()) {
          break;
        } else {
          i = selectedKeys.iterator();
        }
      }
    }
  }

  private void processSelectedKeys() {
    if (selectedKeys != null) {
      processSelectedKeysOptimized();
    } else {
      processSelectedKeysPlain(selector.selectedKeys());
    }
  }

  protected void cleanup() {
    try {
      selector.close();
    } catch (IOException e) {
      logger.warn("Failed to close a selector.", e);
    }
  }

  void cancel(SelectionKey key) throws IOException {
    if (isShuttingDown()) {
      logger.warn("the engine is being shutdown. the cancel operation is ignored");
      return;
    }

    wakeupSelector();
    try {
      key.cancel();
      cancelledKeys++;
      if (cancelledKeys >= CLEANUP_INTERVAL) {
        cancelledKeys = 0;
        needsToSelectAgain = true;
      }
    } finally {
      waitSelectorDone();
    }
  }

  private void closeAll() {
    selectAgain();
    Set<SelectionKey> keys = selector.keys();
    Collection<AbstractNioChannel> channels = new ArrayList<AbstractNioChannel>(keys.size());
    for (SelectionKey k : keys) {
      Object a = k.attachment();
      if (a instanceof AbstractNioChannel) {
        channels.add((AbstractNioChannel) a);
      } else {
        Validate.isTrue(false);
      }
    }

    for (AbstractNioChannel ch : channels) {
      ch.unsafe().close(ch.unsafe().voidPromise());
    }
    synchronizer.close();
  }

  protected boolean confirmShutdown() {
    if (!isShuttingDown()) {
      return false;
    }

    if (!inEventLoop()) {
      throw new IllegalStateException("must be invoked from an event loop");
    }

    cancelScheduledTasks();
    return true;
  }

  public void wakeupSelector() throws IOException {
    synchronizer.clientWakeupServer();
  }

  public void waitSelectorDone() throws IOException {
    synchronizer.clientWaitOnBarrier();
  }

  public boolean inReaderThread() {
    return inReaderThread(Thread.currentThread());
  }

  public boolean inReaderThread(Thread thread) {
    return thread == readerThread;
  }

  public boolean inTaskThread() {
    return inTaskThread(Thread.currentThread());
  }

  public boolean inTaskThread(Thread thread) {
    return taskEngine.inEventLoop(thread);
  }

  private static final class SelectorTuple {
    final Selector unwrappedSelector;
    final Selector selector;
    final SelectedSelectionKeySet keySet;

    SelectorTuple(Selector unwrappedSelector) {
      this.unwrappedSelector = unwrappedSelector;
      this.selector = unwrappedSelector;
      this.keySet = null;
    }

    SelectorTuple(Selector unwrappedSelector, Selector selector, SelectedSelectionKeySet keySet) {
      this.unwrappedSelector = unwrappedSelector;
      this.selector = selector;
      this.keySet = keySet;
    }
  }

  private final class ScheduleTaskPoller implements Runnable {
    protected boolean stop = false;

    @Override
    public void run() {
      while (true) {
        if (stop) {
          break;
        }

        try {
          long nanoTime = AbstractScheduledEventExecutor.nanoTime();
          Runnable scheduledTask = pollScheduledTask(nanoTime);
          while (scheduledTask != null) {
            if (!taskEngine.submit(scheduledTask)) {
              throw new RuntimeException("no space in the task queue");
            }
            scheduledTask = pollScheduledTask(nanoTime);
          }

          Thread.sleep(100);

        } catch (InterruptedException t) {
          logger.warn("Event loop puller is being interrupted");
        } catch (Throwable t) {
          logger.warn("caught an exception when polling scheduled tasks", t);
        }
      }

      logger.warn("the scheduleTaskPoller exit");
    }
  }

  private final class Reader implements Runnable {
    protected boolean stop = false;

    @Override
    public void run() {
      logger.warn("reader-{} is starting", seqId);
      for (; ; ) {
        if (!stop) {
          try {
            selector.select();
            processSelectedKeys();
          } catch (Throwable t) {
            handleLoopException(t);
          } finally {
            synchronizer.serverWaitOnBarrier();
          }
        }

        try {
          if (isShuttingDown()) {
            logger.warn("reader thread is exiting, close all channels related to it");
            closeAll();
            if (confirmShutdown()) {
              logger.warn("reader thread is done");
              return;
            }
            stop = true;

            Thread.sleep(100);
          }
        } catch (Throwable t) {
          handleLoopException(t);
        }
      }
    }
  }
}

