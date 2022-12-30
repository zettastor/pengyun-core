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

package py.common.tlsf.bytebuffer.manager;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.DirectAlignedBufferAllocator;
import py.common.tlsf.BaseTlsfSpaceManager;
import py.common.tlsf.OutOfSpaceException;
import py.common.tlsf.SimpleTlsfMetadata;

/**
 * The instance of this class manages space in a direct byte buffer. It allocates space as a direct
 * byte buffer and releases unused direct byte buffer.
 *
 */
public class TlsfByteBufferManager {
  private static final Logger logger = LoggerFactory.getLogger(TlsfByteBufferManager.class);

  private final BaseTlsfSpaceManager baseTlsfSpaceManager;

  private final ByteBuffer buffer;

  private final long basePhysicalAddr;
  private final AtomicInteger restAllocateAlignedBufferCount;
  private BlockingQueue<Semaphore> releasingEventListeners = new LinkedBlockingQueue<>();

  /**
   * Constructor for this class.
   *
   * @param sizeAlignment  alignment of each allocated size
   * @param size           the total size of direct byte buffer in the manager
   * @param addressAligned a flag represents if the direct byte buffer in manager is
   *                       address-aligned
   */
  public TlsfByteBufferManager(int sizeAlignment, int size, boolean addressAligned) {
    this.buffer = (addressAligned) ? DirectAlignedBufferAllocator.allocateAlignedByteBuffer(size)
        : ByteBuffer.allocateDirect(size);
    this.basePhysicalAddr = DirectAlignedBufferAllocator.getAddress(buffer);
    this.baseTlsfSpaceManager = new BaseTlsfSpaceManager(new SimpleTlsfMetadata(),
        new ByteBufferDivisionMetadata(),
        sizeAlignment, 0L, size);
    this.restAllocateAlignedBufferCount = new AtomicInteger(size);
  }

  /**
   * Allocate direct byte buffer with size largger than or equal to the given size. If the size is
   * aligned, then the size of returned buffer will be equal to the desired size. Otherwise, the
   * size of returned buffer will be largger.
   *
   * @param size desired size
   * @return direct byte buffer with size largger than or equal to the given size
   * @throws OutOfSpaceException      if no more direct byte buffer can be allocated to fit the
   *                                  given size
   * @throws IllegalArgumentException if size is less than or equal to zero
   */
  public synchronized ByteBuffer allocate(int size)
      throws OutOfSpaceException, IllegalArgumentException {
    if (size <= 0) {
      logger.error("Illegal size {}! Size should be positive.", size);
      throw new IllegalArgumentException("Invaid size: " + size);
    }

    int address = (int) baseTlsfSpaceManager.allocate(size);

    buffer.clear();
    buffer.position(address);
    buffer.limit(
        (int) (address + baseTlsfSpaceManager.getDivisionMetadata().getAccessibleMemSize(address)));
    return buffer.slice();
  }

  /**
   * Allocate direct byte buffer with size largger than or equal to the given size until
   * successfully.
   *
   * @param size desired size
   * @return direct byte buffer with size largger than or equal to the given size
   * @throws IllegalArgumentException if size is less than or equal to zero
   */
  public ByteBuffer blockingAllocate(int size) throws IllegalArgumentException {
    Semaphore semaphore = null;
    try {
      while (true) {
        synchronized (this) {
          try {
            return allocate(size);
          } catch (OutOfSpaceException e) {
            if (semaphore == null) {
              semaphore = new Semaphore(0);
            }
            releasingEventListeners.offer(semaphore);
          }
        }

        semaphore.acquireUninterruptibly();
      }
    } finally {
      restAllocateAlignedBufferCount.getAndUpdate(new IntUnaryOperator() {
        @Override
        public int applyAsInt(int operand) {
          return operand - size;
        }
      });
    }
  }

  /**
   * Release the allocated buffer to the manager.
   *
   * @param buffer buffer which was allocated from manager before.
   * @throws IllegalArgumentException if the buffer was not allocated from the manager before
   */
  public synchronized void release(ByteBuffer buffer) throws IllegalArgumentException {
    if (!buffer.isDirect()) {
      logger.error("Illegal byte buffer! Expect a direct byte buffer, but it was not!");
      throw new IllegalArgumentException();
    }

    long physicalAddr = DirectAlignedBufferAllocator.getAddress(buffer);

    int address = (int) (physicalAddr - basePhysicalAddr);
    if (address < 0 || address >= this.buffer.capacity()) {
      logger.error("Illegal byte buffer! Address of it {} is out of bound.", address);
      throw new IllegalArgumentException("Invalid buffer");
    }

    long size = buffer.capacity();
    restAllocateAlignedBufferCount.getAndUpdate(new IntUnaryOperator() {
      @Override
      public int applyAsInt(int operand) {
        return operand + (int) size;
      }
    });
    baseTlsfSpaceManager.release(address);

    while (releasingEventListeners.peek() != null) {
      Semaphore listener = releasingEventListeners.poll();
      listener.release();
    }
  }

  public synchronized List<ByteBuffer> tryAllocate(int size)
      throws OutOfSpaceException, IllegalArgumentException {
    if (size <= 0) {
      logger.error("Illegal size {}! Size should be positive.", size);
      throw new IllegalArgumentException("Invaid size: " + size);
    }

    List<Integer> bufferAddrList = new ArrayList<>();

    long remainingSize = size;
    while (remainingSize > 0) {
      int address;
      try {
        address = (int) baseTlsfSpaceManager.tryAllocate(size);
        bufferAddrList.add(address);
      } catch (OutOfSpaceException e) {
        for (int bufferAddr : bufferAddrList) {
          baseTlsfSpaceManager.release(bufferAddr);
        }
        throw e;
      }

      remainingSize -= baseTlsfSpaceManager.getDivisionMetadata().getAccessibleMemSize(address);
    }

    List<ByteBuffer> bufferList = new ArrayList<>();
    for (int bufferAddr : bufferAddrList) {
      buffer.clear();
      buffer.position(bufferAddr);
      buffer.limit(
          (int) (bufferAddr + baseTlsfSpaceManager.getDivisionMetadata()
              .getAccessibleMemSize(bufferAddr)));
      bufferList.add(buffer.slice());
    }

    return bufferList;
  }

  /**
   * Allocate multiple direct byte buffers to fit the given desired size until successfully. If no
   * more direct byte buffer in manager with size equal to or larger than the desired size, then
   * manager will find buffers with small size to fit the desired size.
   *
   * @param size desired size
   * @return a list of multiple direct byte buffers to fit the given desired size.
   * @throws IllegalArgumentException if size is less than or equal to zero
   */
  public List<ByteBuffer> blockingTryAllocate(int size) throws IllegalArgumentException {
    Semaphore semaphore = null;
    while (true) {
      try {
        return tryAllocate(size);
      } catch (OutOfSpaceException e) {
        if (semaphore == null) {
          semaphore = new Semaphore(0);
        }
        releasingEventListeners.offer(semaphore);
        semaphore.acquireUninterruptibly();
      }
    }
  }

  /**
   * this method must be used for #link blockingAllocate().
   */
  public Integer getSpaceCanBeUsed() {
    return restAllocateAlignedBufferCount.get();
  }
}
