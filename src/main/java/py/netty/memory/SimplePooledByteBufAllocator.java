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

package py.netty.memory;

import io.netty.buffer.AbstractByteBufAllocator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplePooledByteBufAllocator extends AbstractByteBufAllocator {
  private static final Logger logger = LoggerFactory.getLogger(SimplePooledByteBufAllocator.class);
  private static final int DEFAULT_POOL_SIZE = 256 * 1024 * 1024;

  private static final int DEFAULT_MEDIUM_PAGE_SIZE = 8 * 1024;
  private static final int DEFAULT_LITTLE_PAGE_SIZE = 1024;
  private static final int DEFAULT_LARGE_PAGE_SIZE = 128 * 1024;

  public final ConcurrentLinkedQueue<ByteBuf> littleQueue = new ConcurrentLinkedQueue<ByteBuf>();
  public final ConcurrentLinkedQueue<ByteBuf> mediumQueue = new ConcurrentLinkedQueue<ByteBuf>();
  public final ConcurrentLinkedQueue<ByteBuf> largeQueue = new ConcurrentLinkedQueue<ByteBuf>();

  public final ByteBuffer directBuffer;
  public final UnpooledByteBufAllocator allocator = new UnpooledByteBufAllocator(false);

  public final int mediumPageSize;
  public final int mediumMaxOrder;
  public final int littlePageSize;
  public final int littleMaxOrder;
  public final int largePageSize;
  public final int largeMaxOrder;
  public int mediumPageCount;
  public int littlePageCount;
  public int largePageCount;
  private String prefix;
  private boolean align512;

  public SimplePooledByteBufAllocator(String prefix) {
    this(DEFAULT_POOL_SIZE, DEFAULT_MEDIUM_PAGE_SIZE, DEFAULT_LITTLE_PAGE_SIZE,
        DEFAULT_LARGE_PAGE_SIZE, prefix);
  }

  public SimplePooledByteBufAllocator() {
    this(DEFAULT_POOL_SIZE, DEFAULT_MEDIUM_PAGE_SIZE, DEFAULT_LITTLE_PAGE_SIZE,
        DEFAULT_LARGE_PAGE_SIZE, "network");
  }

  public SimplePooledByteBufAllocator(int poolSize, int mediumPageSize) {
    this(poolSize, mediumPageSize, DEFAULT_LITTLE_PAGE_SIZE, DEFAULT_LARGE_PAGE_SIZE, "network");
  }

  public SimplePooledByteBufAllocator(int poolSize, int pageMediumSize, int littlePageSize,
      int largePageSize) {
    this(poolSize, pageMediumSize, littlePageSize, largePageSize, "network");
  }

  public SimplePooledByteBufAllocator(int poolSize, int pageMediumSize, int littlePageSize,
      int largePageSize,
      String prefix) {
    super(true);
    this.prefix = prefix;

    ResourceLeakDetector.setLevel(Level.DISABLED);

    if (pageMediumSize <= littlePageSize) {
      throw new IllegalArgumentException(
          "pageSize: " + pageMediumSize + " (expected: " + littlePageSize + "+)");
    }

    if (pageMediumSize >= largePageSize) {
      throw new IllegalArgumentException(
          "pageSize: " + pageMediumSize + " (expected: " + largePageSize + "-)");
    }

    this.mediumMaxOrder = validateAndCalculatePageShifts(pageMediumSize);
    this.mediumPageSize = pageMediumSize;

    this.littleMaxOrder = validateAndCalculatePageShifts(littlePageSize);
    this.littlePageSize = littlePageSize;

    this.largeMaxOrder = validateAndCalculatePageShifts(largePageSize);
    this.largePageSize = largePageSize;

    this.largePageCount = poolSize >> largeMaxOrder;
    Validate.isTrue(this.largePageCount > 0);
    poolSize = this.largePageCount * largePageSize;
    directBuffer = allocateInitialize(poolSize);

    int position = 0;
    byte[] zeroBuffer = new byte[largePageSize];
    for (int i = 0; i < this.largePageCount; i++) {
      directBuffer.position(position).limit(position + largePageSize);
      largeQueue.offer(new PyPooledDirectByteBuf(this, directBuffer.slice().put(zeroBuffer)));
      position += largePageSize;
    }

    int fromBigCount = (this.largePageCount << mediumMaxOrder) >> largeMaxOrder;
    int mediumPageTimesOfLargePage = (1 << largeMaxOrder) >> mediumMaxOrder;
    this.largePageCount -= fromBigCount;

    while (fromBigCount-- > 0) {
      ByteBuffer buffer = largeQueue.poll().nioBuffer();
      position = 0;
      for (int i = 0; i < mediumPageTimesOfLargePage; i++) {
        buffer.position(position);
        buffer.limit(position + mediumPageSize);
        mediumQueue.offer(new PyPooledDirectByteBuf(this, buffer.slice()));
        position += mediumPageSize;
        this.mediumPageCount++;
      }
    }

    int fromMediumCount = (this.mediumPageCount << littleMaxOrder) >> mediumMaxOrder;
    int littlePageTimesOfMediumPage = (1 << mediumMaxOrder) >> littleMaxOrder;
    this.mediumPageCount -= fromMediumCount;

    while (fromMediumCount-- > 0) {
      ByteBuffer buffer = mediumQueue.poll().nioBuffer();
      position = 0;
      for (int i = 0; i < littlePageTimesOfMediumPage; i++) {
        buffer.position(position);
        buffer.limit(position + littlePageSize);
        littleQueue.offer(new PyPooledDirectByteBuf(this, buffer.slice()));
        position += littlePageSize;
        this.littlePageCount++;
      }
    }

    logger.warn(
        "pool size: {}, big page size: {}. big page count: {}, medium page size: {}, "
            + "medium page count: {}, little page size: {}, little page count: {}",
        poolSize, this.largePageSize, this.largePageCount, this.mediumPageSize,
        this.mediumPageCount,
        this.littlePageSize, this.littlePageCount);
  }

  private static int validateAndCalculatePageShifts(int pageSize) {
    if ((pageSize & pageSize - 1) != 0) {
      throw new IllegalArgumentException("pageSize: " + pageSize + " (expected: power of 2)");
    }

    return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(pageSize);
  }

  protected ByteBuffer allocateInitialize(int poolSize) {
    return ByteBuffer.allocateDirect(poolSize);
  }

  protected ByteBuf allocateFromExternal(int initialCapacity, int maxCapacity) {
    return allocator.buffer(initialCapacity, maxCapacity);
  }

  @Override
  public boolean isDirectBufferPooled() {
    return true;
  }

  public void release(ByteBuf byteBuf) {
    Validate.isTrue(byteBuf instanceof PyPooledDirectByteBuf);
    int capacity = byteBuf.capacity();
    if (capacity < this.mediumPageSize) {
      Validate.isTrue(littleQueue.offer(byteBuf));
    } else if (capacity < this.largePageSize) {
      Validate.isTrue(mediumQueue.offer(byteBuf));
    } else {
      Validate.isTrue(largeQueue.offer(byteBuf));
    }
  }

  @Override
  protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity) {
    throw new UnsupportedOperationException("simple allocator, just for direct buffer");
  }

  @Override
  public ByteBuf ioBuffer() {
    throw new UnsupportedOperationException("simple allocator");
  }

  @Override
  public ByteBuf ioBuffer(int initialCapacity) {
    if (initialCapacity == 0) {
      logger.warn("why need empty bytebuf", new Exception());
    }

    ByteBuf buf = ioBuffer(initialCapacity, initialCapacity);
    Validate.isTrue(buf.writableBytes() >= initialCapacity);
    return buf;
  }

  @Override
  public ByteBuf directBuffer() {
    throw new UnsupportedOperationException("simple allocator");
  }

  @Override
  public ByteBuf directBuffer(int initialCapacity) {
    return directBuffer(initialCapacity, initialCapacity);
  }

  @Override
  protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity) {
    Validate.isTrue(maxCapacity > 0);
    if (maxCapacity < this.mediumPageSize) {
      return allocateFromLittleQueue(initialCapacity, maxCapacity);
    } else if (maxCapacity < this.largePageSize) {
      return allocateFromMediumQueue(initialCapacity, maxCapacity);
    } else {
      return allocateFromLargeQueue(initialCapacity, maxCapacity);
    }

  }

  protected ByteBuf allocateFromLittleQueue(int initialCapacity, int maxCapacity) {
    int pageCount = (maxCapacity + littlePageSize - 1) >> littleMaxOrder;
    if (pageCount == 1) {
      ByteBuf buf = littleQueue.poll();
      if (buf == null) {
        return allocateFromExternal(initialCapacity, maxCapacity);
      } else {
        return buf;
      }
    }

    ByteBuf[] buffers = new ByteBuf[pageCount];
    int i = 0;
    for (; i < pageCount; i++) {
      buffers[i] = littleQueue.poll();
      if (buffers[i] == null) {
        break;
      }

      buffers[i].writerIndex(buffers[i].capacity());
    }

    if (i == pageCount) {
      return new PyCompositeByteBuf(this, true, pageCount, buffers).writerIndex(0);

    } else {
      for (int j = 0; j < i; j++) {
        Validate.isTrue(littleQueue.offer(buffers[j].clear()));
      }
      return allocateFromExternal(initialCapacity, maxCapacity);
    }
  }

  protected ByteBuf allocateFromMediumQueue(int initialCapacity, int maxCapacity) {
    int pageCount = (maxCapacity + mediumPageSize - 1) >> mediumMaxOrder;
    if (pageCount == 1) {
      ByteBuf buf = mediumQueue.poll();
      if (buf == null) {
        return allocateFromExternal(initialCapacity, maxCapacity);
      } else {
        return buf;
      }
    }

    ByteBuf[] buffers = new ByteBuf[pageCount];
    int i = 0;
    for (; i < pageCount; i++) {
      buffers[i] = mediumQueue.poll();
      if (buffers[i] == null) {
        break;
      }

      buffers[i].writerIndex(buffers[i].capacity());
    }

    if (i == pageCount) {
      return new PyCompositeByteBuf(this, true, pageCount, buffers).writerIndex(0);

    } else {
      for (int j = 0; j < i; j++) {
        Validate.isTrue(mediumQueue.offer(buffers[j].clear()));
      }
      return allocateFromExternal(initialCapacity, maxCapacity);
    }
  }

  protected ByteBuf allocateFromLargeQueue(int initialCapacity, int maxCapacity) {
    int pageCount = (maxCapacity + largePageSize - 1) >> largeMaxOrder;
    if (pageCount == 1) {
      ByteBuf buf = largeQueue.poll();
      if (buf == null) {
        return allocateFromExternal(initialCapacity, maxCapacity);
      } else {
        return buf;
      }
    }

    ByteBuf[] buffers = new ByteBuf[pageCount];
    int i = 0;
    for (; i < pageCount; i++) {
      buffers[i] = largeQueue.poll();
      if (buffers[i] == null) {
        break;
      }

      buffers[i].writerIndex(buffers[i].capacity());
    }

    if (i == pageCount) {
      return new PyCompositeByteBuf(this, true, pageCount, buffers).writerIndex(0);

    } else {
      for (int j = 0; j < i; j++) {
        Validate.isTrue(largeQueue.offer(buffers[j].clear()));
      }

      return allocateFromExternal(initialCapacity, maxCapacity);
    }
  }

  public int getTotolPageCount() {
    return mediumPageCount + littlePageCount;
  }

  public int getLittlePageCount() {
    return littlePageCount;
  }

  public int getMediumPageCount() {
    return mediumPageCount;
  }

  public int getLargePageCount() {
    return largePageCount;
  }

  public int getAvailableMediumPageCount() {
    return mediumQueue.size();
  }

  public int getAvailableLittlePageCount() {
    return littleQueue.size();
  }

  public int getAvailableLargePageCount() {
    return largeQueue.size();
  }
}
