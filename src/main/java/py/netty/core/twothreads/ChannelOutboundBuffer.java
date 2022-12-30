/*
 * Copyright 2013 The Netty Project
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

import static java.lang.Math.min;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.channel.FileRegion;
import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.InternalThreadLocalMap;
import io.netty.util.internal.SystemPropertyUtil;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ChannelOutboundBuffer {
  static final int CHANNEL_OUTBOUND_BUFFER_ENTRY_OVERHEAD =
      SystemPropertyUtil.getInt("io.netty.transport.outboundBufferEntrySizeOverhead", 96);

  private static final Logger logger = LoggerFactory.getLogger(ChannelOutboundBuffer.class);

  private static final FastThreadLocal<ByteBuffer[]> NIO_BUFFERS = new
      FastThreadLocal<ByteBuffer[]>() {
        @Override
        protected ByteBuffer[] initialValue() throws Exception {
          return new ByteBuffer[1024];
        }
      };
  private static final AtomicLongFieldUpdater<ChannelOutboundBuffer> TOTAL_PENDING_SIZE_UPDATER =
      AtomicLongFieldUpdater.newUpdater(ChannelOutboundBuffer.class, "totalPendingSize");
  private static final AtomicIntegerFieldUpdater<ChannelOutboundBuffer> UNWRITABLE_UPDATER =
      AtomicIntegerFieldUpdater.newUpdater(ChannelOutboundBuffer.class, "unwritable");
  private final Channel channel;

  private Entry flushedEntry;

  private Entry unflushedEntry;

  private Entry tailEntry;

  private int flushed;
  private int nioBufferCount;
  private long nioBufferSize;
  private boolean inFail;
  @SuppressWarnings("UnusedDeclaration")
  private volatile long totalPendingSize;
  @SuppressWarnings("UnusedDeclaration")
  private volatile int unwritable;

  private volatile Runnable fireChannelWritabilityChangedTask;

  ChannelOutboundBuffer(AbstractChannel channel) {
    this.channel = channel;
  }

  private static long total(Object msg) {
    if (msg instanceof ByteBuf) {
      return ((ByteBuf) msg).readableBytes();
    }
    if (msg instanceof FileRegion) {
      return ((FileRegion) msg).count();
    }
    if (msg instanceof ByteBufHolder) {
      return ((ByteBufHolder) msg).content().readableBytes();
    }
    return -1;
  }

  private static ByteBuffer[] expandNioBufferArray(ByteBuffer[] array, int neededSpace, int size) {
    int newCapacity = array.length;
    do {
      newCapacity <<= 1;

      if (newCapacity < 0) {
        throw new IllegalStateException();
      }

    } while (neededSpace > newCapacity);

    ByteBuffer[] newArray = new ByteBuffer[newCapacity];
    System.arraycopy(array, 0, newArray, 0, size);

    return newArray;
  }

  private static int writabilityMask(int index) {
    if (index < 1 || index > 31) {
      throw new IllegalArgumentException("index: " + index + " (expected: 1~31)");
    }
    return 1 << index;
  }

  private static void safeSuccess(ChannelPromise promise) {
    PromiseLoggingUtil.trySuccess(promise, null, logger);
  }

  private static void safeFail(ChannelPromise promise, Throwable cause) {
    PromiseLoggingUtil.tryFailure(promise, cause, logger);
  }

  public void addMessage(Object msg, int size, ChannelPromise promise) {
    Entry entry = Entry.newInstance(msg, size, total(msg), promise);
    if (tailEntry == null) {
      flushedEntry = null;
    } else {
      Entry tail = tailEntry;
      tail.next = entry;
    }
    tailEntry = entry;
    if (unflushedEntry == null) {
      unflushedEntry = entry;
    }

    incrementPendingOutboundBytes(entry.pendingSize, false);
  }

  public void addFlush() {
    Entry entry = unflushedEntry;
    if (entry != null) {
      if (flushedEntry == null) {
        flushedEntry = entry;
      }
      do {
        flushed++;
        if (!entry.promise.setUncancellable()) {
          int pending = entry.cancel();
          decrementPendingOutboundBytes(pending, false, true);
        }
        entry = entry.next;
      } while (entry != null);

      unflushedEntry = null;
    }
  }

  void incrementPendingOutboundBytes(long size) {
    incrementPendingOutboundBytes(size, true);
  }

  private void incrementPendingOutboundBytes(long size, boolean invokeLater) {
    if (size == 0) {
      return;
    }

    long newWriteBufferSize = TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, size);
    if (newWriteBufferSize > channel.config().getWriteBufferHighWaterMark()) {
      setUnwritable(invokeLater);
    }
  }

  void decrementPendingOutboundBytes(long size) {
    decrementPendingOutboundBytes(size, true, true);
  }

  private void decrementPendingOutboundBytes(long size, boolean invokeLater,
      boolean notifyWritability) {
    if (size == 0) {
      return;
    }

    long newWriteBufferSize = TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, -size);
    if (notifyWritability && newWriteBufferSize < channel.config().getWriteBufferLowWaterMark()) {
      setWritable(invokeLater);
    }
  }

  public Object current() {
    Entry entry = flushedEntry;
    if (entry == null) {
      return null;
    }

    return entry.msg;
  }

  public void progress(long amount) {
    Entry e = flushedEntry;
    assert e != null;
    ChannelPromise p = e.promise;
    if (p instanceof ChannelProgressivePromise) {
      long progress = e.progress + amount;
      e.progress = progress;
      ((ChannelProgressivePromise) p).tryProgress(progress, e.total);
    }
  }

  public boolean remove() {
    Entry e = flushedEntry;
    if (e == null) {
      clearNioBuffers();
      return false;
    }
    Object msg = e.msg;

    ChannelPromise promise = e.promise;
    int size = e.pendingSize;

    removeEntry(e);

    if (!e.cancelled) {
      ReferenceCountUtil.safeRelease(msg);
      safeSuccess(promise);
      decrementPendingOutboundBytes(size, false, true);
    }

    e.recycle();

    return true;
  }

  public boolean remove(Throwable cause) {
    return remove0(cause, true);
  }

  private boolean remove0(Throwable cause, boolean notifyWritability) {
    Entry e = flushedEntry;
    if (e == null) {
      clearNioBuffers();
      return false;
    }
    Object msg = e.msg;

    ChannelPromise promise = e.promise;
    int size = e.pendingSize;

    removeEntry(e);

    if (!e.cancelled) {
      ReferenceCountUtil.safeRelease(msg);

      safeFail(promise, cause);
      decrementPendingOutboundBytes(size, false, notifyWritability);
    }

    e.recycle();

    return true;
  }

  private void removeEntry(Entry e) {
    if (--flushed == 0) {
      flushedEntry = null;
      if (e == tailEntry) {
        tailEntry = null;
        unflushedEntry = null;
      }
    } else {
      flushedEntry = e.next;
    }
  }

  public void removeBytes(long writtenBytes) {
    for (; ; ) {
      Object msg = current();
      if (!(msg instanceof ByteBuf)) {
        assert writtenBytes == 0;
        break;
      }

      final ByteBuf buf = (ByteBuf) msg;
      final int readerIndex = buf.readerIndex();
      final int readableBytes = buf.writerIndex() - readerIndex;

      if (readableBytes <= writtenBytes) {
        if (writtenBytes != 0) {
          progress(readableBytes);
          writtenBytes -= readableBytes;
        }
        remove();
      } else {
        if (writtenBytes != 0) {
          buf.readerIndex(readerIndex + (int) writtenBytes);
          progress(writtenBytes);
        }
        break;
      }
    }
    clearNioBuffers();
  }

  private void clearNioBuffers() {
    int count = nioBufferCount;
    if (count > 0) {
      nioBufferCount = 0;
      Arrays.fill(NIO_BUFFERS.get(), 0, count, null);
    }
  }

  public ByteBuffer[] nioBuffers() {
    return nioBuffers(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  public ByteBuffer[] nioBuffers(int maxCount, long maxBytes) {
    assert maxCount > 0;
    assert maxBytes > 0;
    long nioBufferSize = 0;
    int nioBufferCount = 0;
    final InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();
    ByteBuffer[] nioBuffers = NIO_BUFFERS.get(threadLocalMap);
    Entry entry = flushedEntry;
    while (isFlushedEntry(entry) && entry.msg instanceof ByteBuf) {
      if (!entry.cancelled) {
        ByteBuf buf = (ByteBuf) entry.msg;
        final int readerIndex = buf.readerIndex();
        final int readableBytes = buf.writerIndex() - readerIndex;

        if (readableBytes > 0) {
          if (maxBytes - readableBytes < nioBufferSize && nioBufferCount != 0) {
            break;
          }
          nioBufferSize += readableBytes;
          int count = entry.count;
          if (count == -1) {
            entry.count = count = buf.nioBufferCount();
          }
          int neededSpace = min(maxCount, nioBufferCount + count);
          if (neededSpace > nioBuffers.length) {
            nioBuffers = expandNioBufferArray(nioBuffers, neededSpace, nioBufferCount);
            NIO_BUFFERS.set(threadLocalMap, nioBuffers);
          }
          if (count == 1) {
            ByteBuffer nioBuf = entry.buf;
            if (nioBuf == null) {
              entry.buf = nioBuf = buf.internalNioBuffer(readerIndex, readableBytes);
            }
            nioBuffers[nioBufferCount++] = nioBuf;
          } else {
            ByteBuffer[] nioBufs = entry.bufs;
            if (nioBufs == null) {
              entry.bufs = nioBufs = buf.nioBuffers();
            }
            for (int i = 0; i < nioBufs.length && nioBufferCount < maxCount; ++i) {
              ByteBuffer nioBuf = nioBufs[i];
              if (nioBuf == null) {
                break;
              } else if (!nioBuf.hasRemaining()) {
                continue;
              }
              nioBuffers[nioBufferCount++] = nioBuf;
            }
          }
          if (nioBufferCount == maxCount) {
            break;
          }
        }
      }
      entry = entry.next;
    }
    this.nioBufferCount = nioBufferCount;
    this.nioBufferSize = nioBufferSize;

    return nioBuffers;
  }

  public int nioBufferCount() {
    return nioBufferCount;
  }

  public long nioBufferSize() {
    return nioBufferSize;
  }

  public boolean isWritable() {
    return unwritable == 0;
  }

  private void setWritable(boolean invokeLater) {
    for (; ; ) {
      final int oldValue = unwritable;
      final int newValue = oldValue & ~1;
      if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
        if (oldValue != 0 && newValue == 0) {
          fireChannelWritabilityChanged(invokeLater);
        }
        break;
      }
    }
  }

  public boolean getUserDefinedWritability(int index) {
    return (unwritable & writabilityMask(index)) == 0;
  }

  public void setUserDefinedWritability(int index, boolean writable) {
    if (writable) {
      setUserDefinedWritability(index);
    } else {
      clearUserDefinedWritability(index);
    }
  }

  private void setUserDefinedWritability(int index) {
    final int mask = ~writabilityMask(index);
    for (; ; ) {
      final int oldValue = unwritable;
      final int newValue = oldValue & mask;
      if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
        if (oldValue != 0 && newValue == 0) {
          fireChannelWritabilityChanged(true);
        }
        break;
      }
    }
  }

  private void clearUserDefinedWritability(int index) {
    final int mask = writabilityMask(index);
    for (; ; ) {
      final int oldValue = unwritable;
      final int newValue = oldValue | mask;
      if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
        if (oldValue == 0 && newValue != 0) {
          fireChannelWritabilityChanged(true);
        }
        break;
      }
    }
  }

  private void setUnwritable(boolean invokeLater) {
    for (; ; ) {
      final int oldValue = unwritable;
      final int newValue = oldValue | 1;
      if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
        if (oldValue == 0 && newValue != 0) {
          fireChannelWritabilityChanged(invokeLater);
        }
        break;
      }
    }
  }

  private void fireChannelWritabilityChanged(boolean invokeLater) {
    final ChannelPipeline pipeline = channel.pipeline();
    if (invokeLater) {
      Runnable task = fireChannelWritabilityChangedTask;
      if (task == null) {
        fireChannelWritabilityChangedTask = task = new Runnable() {
          @Override
          public void run() {
            pipeline.fireChannelWritabilityChanged();
          }
        };
      }
      channel.eventLoop().execute(task);
    } else {
      pipeline.fireChannelWritabilityChanged();
    }
  }

  public int size() {
    return flushed;
  }

  public boolean isEmpty() {
    return flushed == 0;
  }

  void failFlushed(Throwable cause, boolean notify) {
    if (inFail) {
      return;
    }

    try {
      inFail = true;
      for (; ; ) {
        if (!remove0(cause, notify)) {
          break;
        }
      }
    } finally {
      inFail = false;
    }
  }

  void close(final Throwable cause, final boolean allowChannelOpen) {
    if (inFail) {
      channel.eventLoop().execute(new Runnable() {
        @Override
        public void run() {
          close(cause, allowChannelOpen);
        }
      });
      return;
    }

    inFail = true;

    if (!allowChannelOpen && channel.isOpen()) {
      throw new IllegalStateException("close() must be invoked after the channel is closed.");
    }

    if (!isEmpty()) {
      throw new IllegalStateException(
          "close() must be invoked after all flushed writes are handled.");
    }

    try {
      Entry e = unflushedEntry;
      while (e != null) {
        int size = e.pendingSize;
        TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, -size);

        if (!e.cancelled) {
          ReferenceCountUtil.safeRelease(e.msg);
          safeFail(e.promise, cause);
        }
        e = e.recycleAndGetNext();
      }
    } finally {
      inFail = false;
    }
    clearNioBuffers();
  }

  void close(ClosedChannelException cause) {
    close(cause, false);
  }

  @Deprecated
  public void recycle() {
  }

  public long totalPendingWriteBytes() {
    return totalPendingSize;
  }

  public long bytesBeforeUnwritable() {
    long bytes = channel.config().getWriteBufferHighWaterMark() - totalPendingSize;

    if (bytes > 0) {
      return isWritable() ? bytes : 0;
    }
    return 0;
  }

  public long bytesBeforeWritable() {
    long bytes = totalPendingSize - channel.config().getWriteBufferLowWaterMark();

    if (bytes > 0) {
      return isWritable() ? 0 : bytes;
    }
    return 0;
  }

  public void forEachFlushedMessage(MessageProcessor processor) throws Exception {
    if (processor == null) {
      throw new NullPointerException("processor");
    }

    Entry entry = flushedEntry;
    if (entry == null) {
      return;
    }

    do {
      if (!entry.cancelled) {
        if (!processor.processMessage(entry.msg)) {
          return;
        }
      }
      entry = entry.next;
    } while (isFlushedEntry(entry));
  }

  private boolean isFlushedEntry(Entry e) {
    return e != null && e != unflushedEntry;
  }

  public interface MessageProcessor {
    boolean processMessage(Object msg) throws Exception;
  }

  static final class Entry {
    private static final Recycler<Entry> RECYCLER = new Recycler<Entry>() {
      @Override
      protected Entry newObject(Handle<Entry> handle) {
        return new Entry(handle);
      }
    };

    private final Handle<Entry> handle;
    Entry next;
    Object msg;
    ByteBuffer[] bufs;
    ByteBuffer buf;
    ChannelPromise promise;
    long progress;
    long total;
    int pendingSize;
    int count = -1;
    boolean cancelled;

    private Entry(Handle<Entry> handle) {
      this.handle = handle;
    }

    static Entry newInstance(Object msg, int size, long total, ChannelPromise promise) {
      Entry entry = RECYCLER.get();
      entry.msg = msg;
      entry.pendingSize = size + CHANNEL_OUTBOUND_BUFFER_ENTRY_OVERHEAD;
      entry.total = total;
      entry.promise = promise;
      return entry;
    }

    int cancel() {
      if (!cancelled) {
        cancelled = true;
        final int psize = pendingSize;

        ReferenceCountUtil.safeRelease(msg);
        msg = Unpooled.EMPTY_BUFFER;

        pendingSize = 0;
        total = 0;
        progress = 0;
        bufs = null;
        buf = null;
        return psize;
      }
      return 0;
    }

    void recycle() {
      next = null;
      bufs = null;
      buf = null;
      msg = null;
      promise = null;
      progress = 0;
      total = 0;
      pendingSize = 0;
      count = -1;
      cancelled = false;
      handle.recycle(this);
    }

    Entry recycleAndGetNext() {
      Entry next = this.next;
      recycle();
      return next;
    }
  }
}
