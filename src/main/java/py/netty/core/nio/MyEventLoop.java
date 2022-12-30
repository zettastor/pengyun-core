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

import io.netty.channel.ChannelException;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopException;
import io.netty.channel.SelectStrategy;
import io.netty.channel.nio.NioTask;
import io.netty.util.IntSupplier;
import io.netty.util.concurrent.RejectedExecutionHandler;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.ReflectionUtil;
import io.netty.util.internal.SystemPropertyUtil;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MyEventLoop extends SingleThreadEventLoop {
  private static final Logger logger = LoggerFactory.getLogger(MyEventLoop.class);

  private static final int CLEANUP_INTERVAL = 256;

  private static final boolean DISABLE_KEYSET_OPTIMIZATION =
      SystemPropertyUtil.getBoolean("io.netty.noKeySetOptimization", false);

  private static final int MIN_PREMATURE_SELECTOR_RETURNS = 3;
  private static final int SELECTOR_AUTO_REBUILD_THRESHOLD;

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

  private final Callable<Integer> pendingTasksCallable = new Callable<Integer>() {
    @Override
    public Integer call() throws Exception {
      return MyEventLoop.super.pendingTasks();
    }
  };
  private final SelectorProvider provider;

  private final AtomicBoolean wakenUp = new AtomicBoolean();
  private final SelectStrategy selectStrategy;

  private Selector selector;
  private final IntSupplier selectNowSupplier = new IntSupplier() {
    @Override
    public int get() throws Exception {
      return selectNow();
    }
  };
  private Selector unwrappedSelector;
  private SelectedSelectionKeySet selectedKeys;
  private volatile int ioRatio = 50;
  private int cancelledKeys;
  private boolean needsToSelectAgain;

  MyEventLoop(MyEventGroup parent, Executor executor, SelectorProvider selectorProvider,
      SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler) {
    super(parent, executor, false, DEFAULT_MAX_PENDING_TASKS, rejectedExecutionHandler);
    if (selectorProvider == null) {
      throw new NullPointerException("selectorProvider");
    }
    if (strategy == null) {
      throw new NullPointerException("selectStrategy");
    }
    provider = selectorProvider;
    final SelectorTuple selectorTuple = openSelector();
    selector = selectorTuple.selector;
    unwrappedSelector = selectorTuple.unwrappedSelector;
    selectStrategy = strategy;
  }

  private static void handleLoopException(Throwable t) {
    logger.warn("Unexpected exception in the selector loop.", t);

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // nothing
    }
  }

  private static void processSelectedKey(SelectionKey k, NioTask<SelectableChannel> task) {
    int state = 0;
    try {
      task.channelReady(k.channel(), k);
      state = 1;
    } catch (Exception e) {
      k.cancel();
      invokeChannelUnregistered(task, k, e);
      state = 2;
    } finally {
      switch (state) {
        case 0:
          k.cancel();
          invokeChannelUnregistered(task, k, null);
          break;
        case 1:

          if (!k.isValid()) {
            invokeChannelUnregistered(task, k, null);
          }
          break;
        default:
          break;
      }
    }
  }

  private void processSelectedKey(SelectionKey k, AbstractNioChannel ch) {
    final AbstractNioChannel.NioUnsafe unsafe = ch.unsafe();
    if (!k.isValid()) {
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

      if ((readyOps & SelectionKey.OP_WRITE) != 0) {
        ch.unsafe().forceFlush();
      }

      if ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0 || readyOps == 0) {
        unsafe.read();
      }
    } catch (CancelledKeyException ignored) {
      unsafe.close(unsafe.voidPromise());
    }
  }

  private static void invokeChannelUnregistered(NioTask<SelectableChannel> task, SelectionKey k,
      Throwable cause) {
    try {
      task.channelUnregistered(k.channel(), cause);
    } catch (Exception e) {
      logger.warn("Unexpected exception while running NioTask.channelUnregistered()", e);
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
      return new SelectorTuple(unwrappedSelector);
    }

    final SelectedSelectionKeySet selectedKeySet = new SelectedSelectionKeySet();

    Object maybeSelectorImplClass = AccessController.doPrivileged(new PrivilegedAction<Object>() {
      @Override
      public Object run() {
        try {
          return Class.forName(
              "sun.nio.ch.SelectorImpl",
              false,
              PlatformDependent.getSystemClassLoader());
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
      selectedKeys = null;
      Exception e = (Exception) maybeException;
      logger.trace("failed to instrument a special java.util.Set into: {}", unwrappedSelector, e);
      return new SelectorTuple(unwrappedSelector);
    }
    selectedKeys = selectedKeySet;
    logger.trace("instrumented a special java.util.Set into: {}", unwrappedSelector);
    return new SelectorTuple(unwrappedSelector,
        new SelectedSelectionKeySetSelector(unwrappedSelector, selectedKeySet));
  }

  public SelectorProvider selectorProvider() {
    return provider;
  }

  @Override
  protected Queue<Runnable> newTaskQueue(int maxPendingTasks) {
    return maxPendingTasks == Integer.MAX_VALUE ? PlatformDependent.<Runnable>newMpscQueue()
        : PlatformDependent.<Runnable>newMpscQueue(maxPendingTasks);
  }

  @Override
  public int pendingTasks() {
    if (inEventLoop()) {
      return super.pendingTasks();
    } else {
      return submit(pendingTasksCallable).syncUninterruptibly().getNow();
    }
  }

  public void register(final SelectableChannel ch, final int interestOps, final NioTask<?> task) {
    if (ch == null) {
      throw new NullPointerException("ch");
    }
    if (interestOps == 0) {
      throw new IllegalArgumentException("interestOps must be non-zero.");
    }
    if ((interestOps & ~ch.validOps()) != 0) {
      throw new IllegalArgumentException(
          "invalid interestOps: " + interestOps + "(validOps: " + ch.validOps() + ')');
    }
    if (task == null) {
      throw new NullPointerException("task");
    }

    if (isShutdown()) {
      throw new IllegalStateException("event loop shut down");
    }

    try {
      ch.register(selector, interestOps, task);
    } catch (Exception e) {
      throw new EventLoopException("failed to register a channel", e);
    }
  }

  public int getIoRatio() {
    return ioRatio;
  }

  public void setIoRatio(int ioRatio) {
    if (ioRatio <= 0 || ioRatio > 100) {
      throw new IllegalArgumentException("ioRatio: " + ioRatio + " (expected: 0 < ioRatio <= 100)");
    }
    this.ioRatio = ioRatio;
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
        SelectionKey newKey = key.channel()
            .register(newSelectorTuple.unwrappedSelector, interestOps, a);
        if (a instanceof AbstractNioChannel) {
          ((AbstractNioChannel) a).selectionKey = newKey;
        }
        nchannels++;
      } catch (Exception e) {
        logger.warn("Failed to re-register a Channel to the new Selector.", e);
        if (a instanceof AbstractNioChannel) {
          AbstractNioChannel ch = (AbstractNioChannel) a;
          ch.unsafe().close(ch.unsafe().voidPromise());
        } else {
          @SuppressWarnings("unchecked")
          NioTask<SelectableChannel> task = (NioTask<SelectableChannel>) a;
          invokeChannelUnregistered(task, key, e);
        }
      }
    }

    selector = newSelectorTuple.selector;
    unwrappedSelector = newSelectorTuple.unwrappedSelector;

    try {
      oldSelector.close();
    } catch (Throwable t) {
      if (logger.isWarnEnabled()) {
        logger.warn("Failed to close the old Selector.", t);
      }
    }

    logger.info("Migrated " + nchannels + " channel(s) to the new Selector.");
  }

  @Override
  protected void run() {
    for (; ; ) {
      try {
        switch (selectStrategy.calculateStrategy(selectNowSupplier, hasTasks())) {
          case SelectStrategy.CONTINUE:
            continue;
          case SelectStrategy.SELECT:
            select(wakenUp.getAndSet(false));

            if (wakenUp.get()) {
              selector.wakeup();
            }
            break;
          default:
        }

        cancelledKeys = 0;
        needsToSelectAgain = false;
        final int ioRatio = this.ioRatio;
        if (ioRatio == 100) {
          try {
            processSelectedKeys();
          } finally {
            runAllTasks();
          }
        } else {
          final long ioStartTime = System.nanoTime();
          try {
            processSelectedKeys();
          } finally {
            final long ioTime = System.nanoTime() - ioStartTime;
            runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
          }
        }
      } catch (Throwable t) {
        handleLoopException(t);
      }

      try {
        if (isShuttingDown()) {
          closeAll();
          if (confirmShutdown()) {
            return;
          }
        }
      } catch (Throwable t) {
        handleLoopException(t);
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

  @Override
  protected void cleanup() {
    try {
      selector.close();
    } catch (IOException e) {
      logger.warn("Failed to close a selector.", e);
    }
  }

  public void cancel(SelectionKey key) {
    key.cancel();
    cancelledKeys++;
    if (cancelledKeys >= CLEANUP_INTERVAL) {
      cancelledKeys = 0;
      needsToSelectAgain = true;
    }
  }

  @Override
  protected Runnable pollTask() {
    Runnable task = super.pollTask();
    if (needsToSelectAgain) {
      selectAgain();
    }
    return task;
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
        @SuppressWarnings("unchecked")
        NioTask<SelectableChannel> task = (NioTask<SelectableChannel>) a;
        processSelectedKey(k, task);
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

  private void processSelectedKeysOptimized() {
    for (int i = 0; i < selectedKeys.size; ++i) {
      final SelectionKey k = selectedKeys.keys[i];

      selectedKeys.keys[i] = null;

      final Object a = k.attachment();

      if (a instanceof AbstractNioChannel) {
        processSelectedKey(k, (AbstractNioChannel) a);
      } else {
        @SuppressWarnings("unchecked")
        NioTask<SelectableChannel> task = (NioTask<SelectableChannel>) a;
        processSelectedKey(k, task);
      }

      if (needsToSelectAgain) {
        selectedKeys.reset(i + 1);

        selectAgain();
        i = -1;
      }
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
        k.cancel();
        @SuppressWarnings("unchecked")
        NioTask<SelectableChannel> task = (NioTask<SelectableChannel>) a;
        invokeChannelUnregistered(task, k, null);
      }
    }

    for (AbstractNioChannel ch : channels) {
      ch.unsafe().close(ch.unsafe().voidPromise());
    }
  }

  @Override
  protected void wakeup(boolean inEventLoop) {
    if (!inEventLoop && wakenUp.compareAndSet(false, true)) {
      selector.wakeup();
    }
  }

  public Selector unwrappedSelector() {
    return unwrappedSelector;
  }

  public int selectNow() throws IOException {
    try {
      return selector.selectNow();
    } finally {
      if (wakenUp.get()) {
        selector.wakeup();
      }
    }
  }

  private void select(boolean oldWakenUp) throws IOException {
    Selector selector = this.selector;
    try {
      int selectCnt = 0;
      long currentTimeNanos = System.nanoTime();
      long selectDeadLineNanos = currentTimeNanos + delayNanos(currentTimeNanos);
      for (; ; ) {
        long timeoutMillis = (selectDeadLineNanos - currentTimeNanos + 500000L) / 1000000L;
        if (timeoutMillis <= 0) {
          if (selectCnt == 0) {
            selector.selectNow();
            selectCnt = 1;
          }
          break;
        }

        if (hasTasks() && wakenUp.compareAndSet(false, true)) {
          selector.selectNow();
          selectCnt = 1;
          break;
        }

        int selectedKeys = selector.select(timeoutMillis);
        selectCnt++;

        if (selectedKeys != 0 || oldWakenUp || wakenUp.get() || hasTasks() || hasScheduledTasks()) {
          break;
        }
        if (Thread.interrupted()) {
          if (logger.isDebugEnabled()) {
            logger.debug("Selector.select() returned prematurely because "
                + "Thread.currentThread().interrupt() was called. Use "
                + "TTEventLoop.shutdownGracefully() to shutdown the TTEventLoop.");
          }
          selectCnt = 1;
          break;
        }

        long time = System.nanoTime();
        if (time - TimeUnit.MILLISECONDS.toNanos(timeoutMillis) >= currentTimeNanos) {
          selectCnt = 1;
        } else if (SELECTOR_AUTO_REBUILD_THRESHOLD > 0
            && selectCnt >= SELECTOR_AUTO_REBUILD_THRESHOLD) {
          logger.warn(
              "Selector.select() returned prematurely {} times in a row; rebuilding Selector {}.",
              selectCnt, selector);

          rebuildSelector();
          selector = this.selector;

          selector.selectNow();
          selectCnt = 1;
          break;
        }

        currentTimeNanos = time;
      }

      if (selectCnt > MIN_PREMATURE_SELECTOR_RETURNS) {
        if (logger.isDebugEnabled()) {
          logger.debug("Selector.select() returned prematurely {} times in a row for Selector {}.",
              selectCnt - 1, selector);
        }
      }
    } catch (CancelledKeyException e) {
      if (logger.isDebugEnabled()) {
        logger.debug(
            CancelledKeyException.class.getSimpleName() + " raised by a Selector {} - JDK bug?",
            selector, e);
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

  private static final class SelectorTuple {
    final Selector unwrappedSelector;
    final Selector selector;

    SelectorTuple(Selector unwrappedSelector) {
      this.unwrappedSelector = unwrappedSelector;
      this.selector = unwrappedSelector;
    }

    SelectorTuple(Selector unwrappedSelector, Selector selector) {
      this.unwrappedSelector = unwrappedSelector;
      this.selector = selector;
    }
  }

}
