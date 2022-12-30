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

package py.common;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.exception.NoAvailableBufferException;

/**
 * An implementation of tlsf(two level segregated fit) memory allocator. The core of this allocator
 * is structure to store buffers and bit map to index buffers.
 *
 */
public class TlsfFastBufferManager implements FastBufferManager {
  protected static final Logger logger = LoggerFactory.getLogger(TlsfFastBufferManager.class);
  protected static final int MAX_BUFFER_QUEUE = 5000;
  /**
   * subdivisions of first level.
   */
  private static final int FIRST_LEVEL_INDEX_COUNT = Long.SIZE;
  /**
   * subdivisions of second level.
   */
  private static final int SECOND_LEVEL_INDEX_COUNT = Long.SIZE;
  /**
   * value for {@code SECOND_LEVEL_INDEX_COUNT} after function log with base 2.
   */
  private static final int SECOND_LEVEL_INDEX_COUNT_LOG2 = Integer
      .numberOfTrailingZeros(SECOND_LEVEL_INDEX_COUNT);
  /**
   * The minimum alignment is 4 bytes.
   */
  private static final int MIN_ALIGNMENT = 4;
  private static final int DEFAULT_RATIO_OF_AVAILABLE_SPACE = 8;
  protected final long nullBufferAddress = Long.MAX_VALUE;
  // the least accessible memory size is total size of next_free_buffer_addr,
  // pre_free_buffer_addr and pre_physical_buffer
  private final long minAccessibleMemSize = 3 * MetadataDescriptor.METADATA_UNIT_BYTES;
  /**
   * this value is always page size.
   */
  private final long alignment;
  private final long firstLevelIndexShift;
  private final long memoryTotalSize;
  private final long memoryBaseAddress;
  private final long memoryEndAddress;
  /**
   * it is used for synchronizing the allocation buffer and release buffer.
   */
  private final ReentrantLock lockForCreateAndRelease = new ReentrantLock(false);
  protected MetadataDescriptor metadataDescriptor;
  protected Queue<FastBuffer> bufferQueue = new LinkedList<FastBuffer>();

  /**
   * bit map for first level.
   */
  private long firstLevelBitMap = 0L;

  /**
   * bit map for second level.
   */
  private long[] secondLevelBitMap = new long[FIRST_LEVEL_INDEX_COUNT];

  /**
   * two-level segregated list.
   */
  private long[][] freeBuffers = new long[FIRST_LEVEL_INDEX_COUNT][SECOND_LEVEL_INDEX_COUNT];
  // The range is 0 - 10
  private int ratioOfAvailableSpace;

  public TlsfFastBufferManager(long totalSize) {
    this(MIN_ALIGNMENT, totalSize);
  }

  public TlsfFastBufferManager(long totalSize, String className) {
    this(MIN_ALIGNMENT, totalSize, DEFAULT_RATIO_OF_AVAILABLE_SPACE, className);
  }

  public TlsfFastBufferManager(int alignment, long totalSize, String className) {
    this(alignment, totalSize, DEFAULT_RATIO_OF_AVAILABLE_SPACE, className);
  }

  public TlsfFastBufferManager(int alignment, long totalSize) {
    this(alignment, totalSize, DEFAULT_RATIO_OF_AVAILABLE_SPACE);
  }

  public TlsfFastBufferManager(int alignment, long totalSize, int ratioOfAvailableBuffer) {
    this(alignment, totalSize, ratioOfAvailableBuffer, TlsfFastBufferManager.class.getSimpleName());
  }

  public TlsfFastBufferManager(int alignment, long totalSize, int ratioOfAvailableBuffer,
      String className) {
    this.ratioOfAvailableSpace = ratioOfAvailableBuffer;
    Validate.isTrue(ratioOfAvailableBuffer <= 10 && ratioOfAvailableBuffer > 0);
    this.alignment = alignment;
    this.firstLevelIndexShift = locateMostLeftOneBit(SECOND_LEVEL_INDEX_COUNT * this.alignment);
    this.memoryTotalSize = totalSize;

    this.memoryBaseAddress = DirectAlignedBufferAllocator.allocateMemory(totalSize);
    this.memoryEndAddress = this.memoryBaseAddress + totalSize - 1;
    this.metadataDescriptor = new MetadataDescriptor();

    for (int i = 0; i < FIRST_LEVEL_INDEX_COUNT; i++) {
      for (int j = 0; j < SECOND_LEVEL_INDEX_COUNT; j++) {
        freeBuffers[i][j] = nullBufferAddress;
      }
    }

    metadataDescriptor.setPrePhysicalAddress(memoryBaseAddress, nullBufferAddress);
    metadataDescriptor.setAddress(memoryBaseAddress);
    // make the allocated memory as a buffer who has metadata and
    // accessible memory. All space are available for user to use
    // except the two metadata fields "size" and "current_phy_address"
    // Note that the metadata pre_physical_address is saved in the end of
    // previous buffer.
    long accessibleMemSize = memoryTotalSize - MetadataDescriptor.BUFFER_METADATA_OVERHEAD
        - MetadataDescriptor.METADATA_UNIT_BYTES;

    metadataDescriptor.setAccessibleMemSize(memoryBaseAddress, accessibleMemSize);
    metadataDescriptor.setFree(memoryBaseAddress);
    // although the big buffer does not have a previous buffer, to unify the
    // process of preUsed bit, set this bit
    metadataDescriptor.setPreUsed(memoryBaseAddress);

    // merge this big buffer to two level segregated list
    // fli: firstLevelIndex
    // Sli: secondLevelIndex
    Pair<Integer, Integer> fliWithSli = mapping(
        metadataDescriptor.getAccessibleMemSize(memoryBaseAddress));
    insertFreeBuffer(memoryBaseAddress, fliWithSli);

    logger.warn("initialize size {} successfully", totalSize);
  }

  private static int locateMostRightOneBit(long value) {
    return Long.numberOfTrailingZeros(value);
  }

  private static int locateMostLeftOneBit(long value) {
    return Long.SIZE - Long.numberOfLeadingZeros(value) - 1;
  }

  @Override
  public FastBuffer allocateBuffer(long size) throws NoAvailableBufferException {
    if (size <= 0) {
      logger.error("Invalid size {}, the requested size to allocate must be positive", size);
      return null;
    }
    // align the requested size to alignment
    long adjustedSize = adjustRequestSize(size);

    FastBuffer fastBuffer = null;
    lockForCreateAndRelease.lock();
    try {
      long targetBufferAddress = pickoutFreeBuffer(adjustedSize);
      if (targetBufferAddress == nullBufferAddress) {
        throw new NoAvailableBufferException();
      }

      prepareBufferForUse(targetBufferAddress, adjustedSize);

      fastBuffer = bufferQueue.poll();
      if (fastBuffer == null) {
        fastBuffer = new FastBufferImpl(targetBufferAddress, size);
      } else {
        fastBuffer = ((FastBufferImpl) fastBuffer).reuse(targetBufferAddress, size);
      }
    } finally {
      lockForCreateAndRelease.unlock();
    }

    return fastBuffer;
  }

  @Override
  public List<FastBuffer> allocateBuffers(long size) throws NoAvailableBufferException {
    long alignment = getAlignmentSize();
    List<FastBuffer> fastBuffers = new ArrayList<FastBuffer>();
    lockForCreateAndRelease.lock();
    try {
      try {
        FastBuffer fastBuffer = allocateBuffer(size);
        fastBuffers.add(fastBuffer);
        return fastBuffers;
      } catch (NoAvailableBufferException e) {
        if (size <= alignment) {
          throw e;
        }
      }

      // It is failure to allocate a buffer in one time, and we want to allocate buffer which size
      // is larger than the alignment. so we should try to allocate buffers which count is the times
      // of the alignment.
      boolean needReleaseBuffer = true;
      try {
        long leftSize = size;

        while (leftSize > 0) {
          long min = Math.min(alignment, leftSize);
          fastBuffers.add(allocateBuffer(min));
          leftSize -= min;
        }

        needReleaseBuffer = false;
        return fastBuffers;
      } catch (Exception e1) {
        throw new NoAvailableBufferException("allocate failure in second time, size: " + size);
      } finally {
        if (needReleaseBuffer) {
          for (FastBuffer fastBuffer : fastBuffers) {
            releaseBuffer(fastBuffer);
          }
        }
      }
    } finally {
      lockForCreateAndRelease.unlock();
    }
  }

  public void releaseBuffer(FastBuffer retbuf) {
    Validate.isTrue(retbuf instanceof FastBufferImpl);
    lockForCreateAndRelease.lock();
    try {
      long address = ((FastBufferImpl) retbuf).accessibleMemAddress
          - MetadataDescriptor.ACCESSIBLE_MEM_OFFESET;
      if (address == nullBufferAddress) {
        logger.warn("The buffer has been released yet");
        return;
      }

      Validate.isTrue(address == metadataDescriptor.getAddress(address));
      retbuf = ((FastBufferImpl) retbuf).reuse(nullBufferAddress, 0);
      if (bufferQueue.size() < MAX_BUFFER_QUEUE) {
        bufferQueue.offer(retbuf);
      }

      metadataDescriptor.setFree(address);
      long nextPhysicalBufferAddress = metadataDescriptor.getNextPhysicalAddress(address);
      if (nextPhysicalBufferAddress != nullBufferAddress && !isEndOfMem(
          nextPhysicalBufferAddress)) {
        metadataDescriptor.setPrePhysicalAddress(nextPhysicalBufferAddress, address);
        metadataDescriptor.setPreFree(nextPhysicalBufferAddress);
      }
      merge(address);
    } finally {
      lockForCreateAndRelease.unlock();
    }
  }

  /**
   * The implementation of mapping function. The function is used for removing from or inserting to
   * two-level segregated list.
   *
   * <p>Mapping function: mapping(size) -> (f, s) <br/> f = low_bound(log2(size)) <br/> s = (size -
   * power(2, f))*power(2, SLI)/power(2, f)
   *
   * @param size size
   * @return a pair of first level index and second level index
   */
  private Pair<Integer, Integer> mapping(long size) {
    int firstLevelIndex = 0;
    int secondLevelIndex = 0;

    if (size < SECOND_LEVEL_INDEX_COUNT * alignment) {
      secondLevelIndex = (int) (size / alignment);
    } else {
      firstLevelIndex = locateMostLeftOneBit(size);
      secondLevelIndex = (int) (((size >> (firstLevelIndex - SECOND_LEVEL_INDEX_COUNT_LOG2)) ^ (1
          << SECOND_LEVEL_INDEX_COUNT_LOG2)));
      // no use of buffer whose size is smaller than most small buffer
      // size
      firstLevelIndex -= (firstLevelIndexShift - 1);
    }

    return new MutablePair<Integer, Integer>(firstLevelIndex, secondLevelIndex);
  }

  /**
   * After get result of first level index and second level index, a suitable buffer may not locate
   * at the position, we need a search for it.
   *
   * @return the metadata of suitable buffer
   */
  private long searchSuitableBuffer(Pair<Integer, Integer> fliWithSli) {
    int firstLevelIndex = fliWithSli.getLeft();
    int secondLevelIndex = fliWithSli.getRight();

    long secondLevelMap = secondLevelBitMap[firstLevelIndex] & (~0L << secondLevelIndex);
    // the bit in first level bit map is empty
    if (secondLevelMap == 0) {
      long firstLevelMap = firstLevelBitMap & (~0L << (firstLevelIndex + 1));

      if (firstLevelMap == 0) {
        return nullBufferAddress;
      }

      firstLevelIndex = locateMostRightOneBit(firstLevelMap);
      secondLevelMap = secondLevelBitMap[firstLevelIndex];
    }

    secondLevelIndex = locateMostRightOneBit(secondLevelMap);

    ((MutablePair<Integer, Integer>) fliWithSli).setLeft(firstLevelIndex);
    ((MutablePair<Integer, Integer>) fliWithSli).setRight(secondLevelIndex);

    return freeBuffers[firstLevelIndex][secondLevelIndex];
  }

  private void removeFreeBuffer(long curFreeBufferAddress, Pair<Integer, Integer> fliWithSli) {
    Validate.isTrue(curFreeBufferAddress != nullBufferAddress);
    if (!metadataDescriptor.isFree(curFreeBufferAddress)) {
      logger.error("the buffer addressed at {} is not free. {}, {}, {}, {}, {} ",
          curFreeBufferAddress,
          metadataDescriptor.getAccessibleMemSize(curFreeBufferAddress),
          metadataDescriptor.getNextFreeAddress(curFreeBufferAddress),
          metadataDescriptor.getNextPhysicalAddress(curFreeBufferAddress),
          metadataDescriptor.getPreFreeAddress(curFreeBufferAddress),
          metadataDescriptor.getPrePhysicalAddress(curFreeBufferAddress));
      Validate.isTrue(metadataDescriptor.isFree(curFreeBufferAddress));
    }

    long preFreeBufferAddress = metadataDescriptor.getPreFreeAddress(curFreeBufferAddress);
    long nextFreeBufferAddress = metadataDescriptor.getNextFreeAddress(curFreeBufferAddress);

    if (preFreeBufferAddress != nullBufferAddress) {
      metadataDescriptor.setNextFreeAddress(preFreeBufferAddress, nextFreeBufferAddress);
    }
    if (nextFreeBufferAddress != nullBufferAddress) {
      metadataDescriptor.setPreFreeAddress(nextFreeBufferAddress, preFreeBufferAddress);
    }

    int firstLevelIndex = fliWithSli.getLeft();
    int secondLevelIndex = fliWithSli.getRight();
    // current free buffer is the head of segregated list
    if (preFreeBufferAddress == nullBufferAddress) {
      freeBuffers[firstLevelIndex][secondLevelIndex] = nextFreeBufferAddress;

      // current free buffer is the end of segregated list, and the list
      // will be empty after remove current buffer
      if (nextFreeBufferAddress == nullBufferAddress) {
        // empty list
        secondLevelBitMap[firstLevelIndex] &= ~(1L << secondLevelIndex);

        // mark first level list empty
        if (secondLevelBitMap[firstLevelIndex] == 0) {
          firstLevelBitMap &= ~(1L << firstLevelIndex);
        }

      }
    }
  }

  /**
   * Insert the free buffer metadata to head of segregated list.
   */
  private void insertFreeBuffer(long address, Pair<Integer, Integer> fliWithSli) {
    int firstLevelIndex = fliWithSli.getLeft();
    int secondLevelIndex = fliWithSli.getRight();

    // put current buffer in head of two level segregated list
    long headBufferAddress = freeBuffers[firstLevelIndex][secondLevelIndex];
    if (headBufferAddress != nullBufferAddress) {
      metadataDescriptor.setPreFreeAddress(headBufferAddress, address);
    }

    metadataDescriptor.setNextFreeAddress(address, headBufferAddress);
    metadataDescriptor.setPreFreeAddress(address, nullBufferAddress);
    freeBuffers[firstLevelIndex][secondLevelIndex] = address;

    // set the two bit map relative position not empty
    firstLevelBitMap |= (1L << firstLevelIndex);
    secondLevelBitMap[firstLevelIndex] |= (1L << secondLevelIndex);
  }

  private long absorb(long preBufferAddress, long address) {
    Validate.isTrue(preBufferAddress != nullBufferAddress);
    Validate.isTrue(address != nullBufferAddress);

    long preBufferSize = metadataDescriptor.getAccessibleMemSize(preBufferAddress);
    long curBufferSize = metadataDescriptor.getAccessibleMemSize(address);
    long newSize = preBufferSize + curBufferSize + MetadataDescriptor.BUFFER_METADATA_OVERHEAD;

    metadataDescriptor.setAccessibleMemSize(preBufferAddress, newSize);

    long nextPhysicalBufferAddress = metadataDescriptor.getNextPhysicalAddress(address);
    if (nextPhysicalBufferAddress != nullBufferAddress && !isEndOfMem(
        nextPhysicalBufferAddress)) {
      metadataDescriptor.setPrePhysicalAddress(nextPhysicalBufferAddress, preBufferAddress);
      metadataDescriptor.setPreFree(nextPhysicalBufferAddress);
    }

    return preBufferAddress;
  }

  /**
   * merge current buffer with previous buffer.
   */
  private long mergePre(long address) {
    Validate.isTrue(address != nullBufferAddress);
    Validate.isTrue(metadataDescriptor.isFree(address));

    if (metadataDescriptor.isPreFree(address)) {
      long prePhysicalBufferAddress = metadataDescriptor.getPrePhysicalAddress(address);
      long preBufferSize = metadataDescriptor.getAccessibleMemSize(prePhysicalBufferAddress);

      Pair<Integer, Integer> fliWithSli = mapping(preBufferSize);
      removeFreeBuffer(prePhysicalBufferAddress, fliWithSli);
      address = absorb(prePhysicalBufferAddress, address);
    }

    return address;
  }

  private long mergeNext(long address) {
    Validate.isTrue(address != nullBufferAddress);
    Validate.isTrue(metadataDescriptor.isFree(address));

    long nextPhysicalBufferAddress = metadataDescriptor.getNextPhysicalAddress(address);
    if (nextPhysicalBufferAddress != nullBufferAddress && !isEndOfMem(nextPhysicalBufferAddress)
        && metadataDescriptor.isFree(nextPhysicalBufferAddress)) {
      Pair<Integer, Integer> fliWithSli = mapping(metadataDescriptor
          .getAccessibleMemSize(nextPhysicalBufferAddress));
      removeFreeBuffer(nextPhysicalBufferAddress, fliWithSli);
      address = absorb(address, nextPhysicalBufferAddress);
    }

    return address;
  }

  protected void merge(long address) {
    address = mergePre(address);
    address = mergeNext(address);

    Pair<Integer, Integer> fliWithSli = mapping(metadataDescriptor.getAccessibleMemSize(address));
    insertFreeBuffer(address, fliWithSli);
  }

  /**
   * Split the buffer to two parts: one part is to allocate out, one is remaining.
   */
  private long splitBuffer(long address, long size) {
    Validate.isTrue(address != nullBufferAddress);
    Validate.isTrue(metadataDescriptor.isFree(address));
    Validate.isTrue(metadataDescriptor.getAccessibleMemSize(address) >= size
        + MetadataDescriptor.METADATA_SIZE_BYTES);

    long remainingBufferMetadataAddress = address + MetadataDescriptor.ACCESSIBLE_MEM_OFFESET
        + (size - MetadataDescriptor.METADATA_UNIT_BYTES);
    long remainingAccessableMemSize = metadataDescriptor.getAccessibleMemSize(address)
        - (size + MetadataDescriptor.BUFFER_METADATA_OVERHEAD);

    metadataDescriptor.setPrePhysicalAddress(remainingBufferMetadataAddress, address);
    metadataDescriptor.setAddress(remainingBufferMetadataAddress);
    metadataDescriptor
        .setAccessibleMemSize(remainingBufferMetadataAddress, remainingAccessableMemSize);
    metadataDescriptor.setFree(remainingBufferMetadataAddress);
    metadataDescriptor.setPreFree(remainingBufferMetadataAddress);

    long nextPhysicalBufferAddress = metadataDescriptor.getNextPhysicalAddress(address);
    if (nextPhysicalBufferAddress != nullBufferAddress && !isEndOfMem(
        nextPhysicalBufferAddress)) {
      // the buffer is not end buffer of memory
      metadataDescriptor
          .setPrePhysicalAddress(nextPhysicalBufferAddress, remainingBufferMetadataAddress);
      metadataDescriptor.setPreFree(nextPhysicalBufferAddress);
    }

    metadataDescriptor.setAccessibleMemSize(address, size);

    return remainingBufferMetadataAddress;
  }

  /**
   * remove remaining buffer from current buffer.
   */
  private void trimFreeBuffer(long address, long size) {
    // check if the buffer can split
    if (metadataDescriptor.getAccessibleMemSize(address)
        >= size + MetadataDescriptor.METADATA_SIZE_BYTES) {
      long remainingBufferAddress = splitBuffer(address, size);
      Pair<Integer, Integer> fliWithSli = mapping(
          metadataDescriptor.getAccessibleMemSize(remainingBufferAddress));
      insertFreeBuffer(remainingBufferAddress, fliWithSli);
    }
  }

  protected long pickoutFreeBuffer(long size) {
    // make the size biggest in list
    if (size >= (1L << SECOND_LEVEL_INDEX_COUNT_LOG2)) {
      long round = (1L << (locateMostLeftOneBit(size) - SECOND_LEVEL_INDEX_COUNT_LOG2)) - 1;
      size += round;
    }

    Pair<Integer, Integer> fliWithSli = mapping(size);
    long suitableBufferAddress = searchSuitableBuffer(fliWithSli);

    if (suitableBufferAddress != nullBufferAddress) {
      removeFreeBuffer(suitableBufferAddress, fliWithSli);
    }

    return suitableBufferAddress;
  }

  protected void prepareBufferForUse(long address, long size) {
    Validate.isTrue(address != nullBufferAddress);

    trimFreeBuffer(address, size);

    long nextPhysicalBufferAddress = metadataDescriptor.getNextPhysicalAddress(address);
    if (nextPhysicalBufferAddress != nullBufferAddress && !isEndOfMem(
        nextPhysicalBufferAddress)) {
      metadataDescriptor.setPreUsed(nextPhysicalBufferAddress);
    }

    metadataDescriptor.setUsed(address);
  }

  private long roundUp(long size) {
    if (size >= (1 << SECOND_LEVEL_INDEX_COUNT_LOG2)) {
      final long round = (1 << (locateMostLeftOneBit(size) - SECOND_LEVEL_INDEX_COUNT_LOG2)) - 1;
      size += round;
    }

    return size;
  }

  protected boolean isEndOfMem(long address) {
    return address + MetadataDescriptor.METADATA_UNIT_BYTES > this.memoryEndAddress;
  }

  protected long adjustRequestSize(long size) {
    return Math.max(alignUp(size), minAccessibleMemSize);
  }

  private long alignUp(long size) {
    return (size + alignment - 1) & ~(alignment - 1);
  }

  private long alignDown(long size) {
    return size - (size & (alignment - 1));
  }

  public void printFliSli() {
  }

  public long getFirstAccessibleMemSize() {
    return metadataDescriptor.getAccessibleMemSize(memoryBaseAddress);
  }

  @Override
  public void close() {
  }

  @Override
  public long size() {
    return (memoryTotalSize * ratioOfAvailableSpace) / 10;
  }

  @Override
  public long getAlignmentSize() {
    return alignment;
  }
}
