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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.exception.BufferOverflowException;
import py.exception.BufferUnderflowException;
import py.exception.NoAvailableBufferException;
import sun.nio.ch.DirectBuffer;

/**
 * An implementation of tlsf(two level segregated fit) memory allocator. The core of this allocator
 * is structure to store buffers and bit map to index buffers.
 */
@SuppressWarnings("restriction")
public class TlsfFileBufferManager implements FastBufferManager {
  private static final Logger logger = LoggerFactory.getLogger(TlsfFileBufferManager.class);

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

  private static final Map<Long, Long> metadataStore = new HashMap<Long, Long>();
  private final long nullBufferAddress = Long.MAX_VALUE;
  // the least accessible memory size is total size of next_free_buffer_addr,
  // pre_free_buffer_addr and pre_physical_buffer
  private final long minAccessibleMemSize = 3 * MetadataDescriptor.METADATA_UNIT_BYTES;
  private final long maxBufferQueue = 5000;
  /**
   * this value is always page size.
   */
  private final long alignment;
  private final long firstLevelIndexShift;
  private final long memoryTotalSize;
  private final long memoryBaseAddress;
  private final long memoryEndAddress;
  /**
   * the max size of a byte buffer is Integer.MAX_VALUE. on the other hand, this manager needs to
   * manager than Integer.MAX_VALUE buffer. Therefore, we use a FileChannel instance instead.
   */
  private RandomAccessFile raf;
  private MetadataDescriptor metadataDescriptor;
  private Bidder refuseController = new NonController();
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
  private Queue<FastBuffer> bufferQueue = new LinkedList<FastBuffer>();

  public TlsfFileBufferManager(String fileBufferPath, long totalSize, long baseAddress)
      throws Exception {
    this(MIN_ALIGNMENT, fileBufferPath, totalSize, baseAddress, true);
  }

  public TlsfFileBufferManager(String fileBufferPath, long totalSize, long baseAddress,
      boolean isDisk)
      throws Exception {
    this(MIN_ALIGNMENT, fileBufferPath, totalSize, baseAddress, isDisk);
  }

  public TlsfFileBufferManager(int alignment, String fileBufferPath, long totalSize,
      long baseAddress, boolean isDisk)
      throws Exception {
    this.alignment = alignment;
    this.firstLevelIndexShift = locateMostLeftOneBit(SECOND_LEVEL_INDEX_COUNT * this.alignment);
    this.memoryTotalSize = totalSize;

    if (isDisk) {
      this.raf = new RandomAccessFile(new File(fileBufferPath), "rw");
    } else {
      this.raf = makeMemoryMappedFile(fileBufferPath, totalSize);
    }

    this.memoryBaseAddress = baseAddress;
    this.memoryEndAddress = this.memoryBaseAddress + totalSize - 1;
    metadataDescriptor = new MetadataDescriptor();

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

    logger.debug("initialize size {} successfully", totalSize);
  }

  private static int locateMostRightOneBit(long value) {
    return Long.numberOfTrailingZeros(value);
  }

  private static int locateMostLeftOneBit(long value) {
    return Long.SIZE - Long.numberOfLeadingZeros(value) - 1;
  }

  /**
   * create a memory file and map it to a list of byte buffer. If there are not enough memory, an
   * NoAvailableBufferException is thrown.
   */
  private RandomAccessFile makeMemoryMappedFile(String fileBufferPath, long totalSize)
      throws Exception {
    try {
      File memoryFile = new File(fileBufferPath);
      long memoryFileSize = Utils.createOrExtendFile(memoryFile, totalSize);
      Validate.isTrue(memoryFileSize == totalSize);

      return new RandomAccessFile(memoryFile, "rw");
    } catch (Exception e) {
      logger.error("caught an exception when creating a file buffer {} ", fileBufferPath, e);
      throw e;
    }
  }

  @Override
  public synchronized FastBuffer allocateBuffer(long size) throws NoAvailableBufferException {
    if (size <= 0) {
      logger.error("Invalid size {}, the requested size to allocate must be positive", size);
      return null;
    }

    long adjustedSize = Math.max(size, minAccessibleMemSize);
    // align the requested size to alignment
    adjustedSize = adjustRequestSize(adjustedSize);

    long targetBufferAddress = pickoutFreeBuffer(adjustedSize);
    if (targetBufferAddress == nullBufferAddress) {
      logger.info("Unable to find a buffer fit given size {}", size);
      throw new NoAvailableBufferException();
    }

    prepareBufferForUse(targetBufferAddress, adjustedSize);

    FastBuffer fastBuffer = bufferQueue.poll();
    if (fastBuffer == null) {
      fastBuffer = new FileFastBufferImpl(targetBufferAddress, size);
    } else {
      fastBuffer = ((FileFastBufferImpl) fastBuffer).reuse(targetBufferAddress, size);
    }

    return fastBuffer;
  }

  @Override
  public synchronized void releaseBuffer(FastBuffer retbuf) {
    Validate.isTrue(retbuf instanceof FileFastBufferImpl);

    long address = ((FileFastBufferImpl) retbuf).accessibleMemAddress
        - MetadataDescriptor.ACCESSIBLE_MEM_OFFESET;
    if (address == nullBufferAddress) {
      logger.warn("The buffer has been released yet");
      return;
    }
    Validate.isTrue(address == metadataDescriptor.getAddress(address));

    ((FileFastBufferImpl) retbuf).reuse(nullBufferAddress, 0);
    if (bufferQueue.size() < maxBufferQueue) {
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

  }

  /**
   * The implementation of mapping function. The function is used for removing from or inserting to
   * two-level segregated list.
   *
   * <p>Mapping function: mapping(size) -> (f, s) <br/> f = low_bound(log2(size)) <br/> s = (size -
   * power(2, f))*power(2, SLI)/power(2, f).
   *
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
        logger.info("No suitable buffer found");
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

    metadataDescriptor.clearMetadata(address);
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

  private void merge(long address) {
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

  private long pickoutFreeBuffer(long size) {
    // make the size biggest in list
    if (size >= (1L << SECOND_LEVEL_INDEX_COUNT_LOG2)) {
      long round = (1L << (locateMostLeftOneBit(size) - SECOND_LEVEL_INDEX_COUNT_LOG2)) - 1;
      size += round;
    }

    Pair<Integer, Integer> fliWithSli = mapping(size);
    long suitableBufferAddress = searchSuitableBuffer(fliWithSli);

    if (suitableBufferAddress == nullBufferAddress) {
      // count times for failure to search suitable buffer
      refuseController.hit();
    } else {
      // count times for success to search suitable buffer
      refuseController.miss();
    }

    // control allocate times by bid base on failure to search buffer before
    if (refuseController.bid()) {
      logger.info("Refuse allocate buffer this time");
      suitableBufferAddress = nullBufferAddress;
    }

    if (suitableBufferAddress != nullBufferAddress) {
      removeFreeBuffer(suitableBufferAddress, fliWithSli);
    }

    return suitableBufferAddress;
  }

  private void prepareBufferForUse(long address, long size) {
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

  private boolean isEndOfMem(long address) {
    return address + MetadataDescriptor.METADATA_UNIT_BYTES > this.memoryEndAddress;
  }

  private long adjustRequestSize(long size) {
    return alignUp(size);
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

  public Bidder getRefuseController() {
    return refuseController;
  }

  public void setRefuseController(Bidder refuseController) {
    this.refuseController = refuseController;
  }

  @Override
  public void close() {
    if (raf != null) {
      try {
        raf.close();
      } catch (IOException e) {
        logger.warn("can't close the internal memory file. Do nothing");
      }
    }
  }

  @Override
  public long size() {
    return memoryTotalSize;
  }

  @Override
  public long getAlignmentSize() {
    return MIN_ALIGNMENT;
  }

  @Override
  public List<FastBuffer> allocateBuffers(long size) throws NoAvailableBufferException {
    throw new NotImplementedException("this file fast buffer");
  }

  /**
   * An interface to bid for determine to do next or not.
   *
   */
  public static interface Bidder {
    /**
     * bid to check if hit or miss.
     */
    public boolean bid();

    /**
     * hit target after bid.
     */
    public void hit();

    /**
     * miss target after bid.
     */
    public void miss();
  }

  /**
   * A class implements interface {@link Bidder} by doing nothing.
   */
  public static final class NonController implements Bidder {
    @Override
    public boolean bid() {
      return false;
    }

    @Override
    public void hit() {
      // do nothing
    }

    @Override
    public void miss() {
      // do nothing
    }

  }

  public static final class Controller implements Bidder {
    private static final Random random = new Random(System.currentTimeMillis());

    private static final int bidRange = 10000;

    private final int subjectiveProbability;

    private final int statisticsDuration;

    private Queue<Integer> window = new LinkedList<Integer>();

    private int hitCounter = 0;

    public Controller(int probability, int duration) {
      Validate.isTrue(duration > 0);

      this.subjectiveProbability = probability;
      this.statisticsDuration = duration;

      for (int i = 0; i < statisticsDuration; i++) {
        window.offer(0);
      }
    }

    @Override
    public void hit() {
      hitCounter -= window.poll();
      window.offer(1);
      ++hitCounter;
    }

    @Override
    public void miss() {
      hitCounter -= window.poll();
      window.offer(0);
    }

    @Override
    public boolean bid() {
      int objectiveProbability = hitCounter * 100 / window.size();
      int probability = Math.max(subjectiveProbability, objectiveProbability);
      Validate.isTrue(probability < 100);

      return random.nextInt(bidRange) < bidRange * probability / 100;
    }
  }

  /**
   * There is a metadata for each buffer allocated by tlsf.
   *
   * <p>The metadata looks like below:
   *
   * <p>|-------pre_phy_buffer-----|
   *
   * <p>|-------cur_phy_buffer-----|
   *
   * <p>|-------size field-----P-F-|
   *
   * <p>|-------next_free----------|
   *
   * <p>|-------pre_free-----------|
   */
  private class MetadataDescriptor {
    /**
     * Members of buffer metadata are all type of {@link Long}, we say the size of each member unit
     * bytes.
     */
    static final int METADATA_UNIT_BYTES = Long.SIZE / Byte.SIZE;

    /**
     * There are 5 members in metadata.
     */
    static final int METADATA_SIZE_BYTES = 5 * METADATA_UNIT_BYTES;

    /**
     * After allocate a buffer to user, two member of buffer metadata is not available for user to
     * use. One is the address of the buffer, another is the size of accessible memory. Other than
     * that, 3 metadata fields can be used by users
     */
    static final int BUFFER_METADATA_OVERHEAD = 2 * METADATA_UNIT_BYTES;

    /**
     * buffer-free bit in size field, which indicates if the current buffer is free or allocated.
     */
    static final long BUFFER_FREE_BIT = 1L << 0;

    /**
     * pre-buffer-free bit in size field, which indicates if the pre-buffer is free or allocated
     * Note that this bit can be set when pre-buffer is released to the manager.
     */
    static final long PRE_BUFFER_FREE_BIT = 1L << 1;

    /**
     * Metadata members offset.
     */
    static final int PRE_ADDRESS_OFFSET = 0;
    static final int ADDRESS_OFFSET = PRE_ADDRESS_OFFSET + METADATA_UNIT_BYTES;
    static final int SIZE_OFFSET = ADDRESS_OFFSET + METADATA_UNIT_BYTES;
    static final int PRE_FREE_ADDRESS_OFFSET = SIZE_OFFSET + METADATA_UNIT_BYTES;
    static final int NEXT_FREE_ADDRESS_OFFSET = PRE_FREE_ADDRESS_OFFSET + METADATA_UNIT_BYTES;
    /**
     * Accessible memory start behind metadata.
     */
    static final int ACCESSIBLE_MEM_OFFESET = PRE_FREE_ADDRESS_OFFSET;

    long getPrePhysicalAddress(long address) {
      return getLong(address + PRE_ADDRESS_OFFSET);
    }

    void setPrePhysicalAddress(long address, long prePhysicalAddress) {
      putLong(address + PRE_ADDRESS_OFFSET, prePhysicalAddress);
    }

    long getNextPhysicalAddress(long address) {
      return address + ACCESSIBLE_MEM_OFFESET + getAccessibleMemSize(address) - METADATA_UNIT_BYTES;
    }

    long getAddress(long address) {
      return getLong(address + ADDRESS_OFFSET);
    }

    void setAddress(long address) {
      putLong(address + ADDRESS_OFFSET, address);
    }

    long getAccessibleMemSize(long address) {
      long sizeField = getLong(address + SIZE_OFFSET);
      return sizeField & ~(BUFFER_FREE_BIT | PRE_BUFFER_FREE_BIT);
    }

    void setAccessibleMemSize(long address, long accessibleMemSize) {
      long sizeField = getLong(address + SIZE_OFFSET);
      sizeField = accessibleMemSize | (sizeField & (BUFFER_FREE_BIT | PRE_BUFFER_FREE_BIT));
      putLong(address + SIZE_OFFSET, sizeField);
    }

    long getPreFreeAddress(long address) {
      return getLong(address + PRE_FREE_ADDRESS_OFFSET);
    }

    void setPreFreeAddress(long address, long preFreeAddress) {
      putLong(address + PRE_FREE_ADDRESS_OFFSET, preFreeAddress);
    }

    long getNextFreeAddress(long address) {
      return getLong(address + NEXT_FREE_ADDRESS_OFFSET);
    }

    void setNextFreeAddress(long address, long nextFreeAddress) {
      putLong(address + NEXT_FREE_ADDRESS_OFFSET, nextFreeAddress);
    }

    boolean isFree(long address) {
      long sizeField = getLong(address + SIZE_OFFSET);
      return (sizeField & BUFFER_FREE_BIT) != 0;
    }

    void setFree(long address) {
      long sizeField = getLong(address + SIZE_OFFSET);
      sizeField |= BUFFER_FREE_BIT;
      putLong(address + SIZE_OFFSET, sizeField);
    }

    boolean isPreFree(long address) {
      long sizeField = getLong(address + SIZE_OFFSET);
      return (sizeField & PRE_BUFFER_FREE_BIT) != 0;
    }

    void setPreFree(long address) {
      long sizeField = getLong(address + SIZE_OFFSET);
      sizeField |= PRE_BUFFER_FREE_BIT;
      putLong(address + SIZE_OFFSET, sizeField);
    }

    void setUsed(long address) {
      long sizeField = getLong(address + SIZE_OFFSET);
      sizeField &= ~BUFFER_FREE_BIT;
      putLong(address + SIZE_OFFSET, sizeField);
    }

    void setPreUsed(long address) {
      long sizeField = getLong(address + SIZE_OFFSET);
      sizeField &= ~PRE_BUFFER_FREE_BIT;
      putLong(address + SIZE_OFFSET, sizeField);
    }

    void clearMetadata(long address) {
      metadataStore.remove(address + PRE_ADDRESS_OFFSET);
      metadataStore.remove(address + ADDRESS_OFFSET);
      metadataStore.remove(address + SIZE_OFFSET);
      metadataStore.remove(address + PRE_FREE_ADDRESS_OFFSET);
      metadataStore.remove(address + NEXT_FREE_ADDRESS_OFFSET);
    }

    private long getLong(long position) {
      Long value = metadataStore.get(position);
      return (value == null) ? 0 : value;
    }

    private void putLong(long position, long value) {
      metadataStore.put(position, value);
    }

  }

  public class FileFastBufferImpl implements FastBuffer {
    /**
     * address of assessible memory address which is behind metadata address.
     */
    private long accessibleMemAddress = 0L;
    /**
     * size of accessible memory size. This is one of the two persistent data in buffer.
     */
    private long accessibleMemSize = 0L;

    private FileFastBufferImpl(long address, long size) {
      this.accessibleMemAddress = address + MetadataDescriptor.ACCESSIBLE_MEM_OFFESET;
      this.accessibleMemSize = size;
    }

    /**
     * Use this method to reuse the object of {@link FastBuffer} to alleviate effort of jvm to
     * create a new instance of a class and free it.
     */
    private FileFastBufferImpl reuse(long address, long size) {
      this.accessibleMemAddress = address + MetadataDescriptor.ACCESSIBLE_MEM_OFFESET;
      this.accessibleMemSize = size;

      return this;
    }

    @Override
    public void get(byte[] dst) throws BufferUnderflowException {
      get(dst, 0, dst.length);
    }

    @Override
    public void get(byte[] dst, int offset, int length) throws BufferUnderflowException {
      get(0, dst, offset, length);
    }

    @Override
    public void get(long srcOffset, byte[] dst, int dstOffset, int length)
        throws BufferUnderflowException {
      if (srcOffset + length > accessibleMemSize) {
        throw new BufferUnderflowException(
            "dst' length " + length + " is larger than the buffer size "
                + accessibleMemSize + ", offset: " + srcOffset);
      }

      synchronized (raf) {
        try {
          raf.seek(accessibleMemAddress + srcOffset);
          raf.read(dst, dstOffset, length);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

    @Override
    public void get(ByteBuffer dst) throws BufferUnderflowException {
      get(0, dst, dst.position(), dst.remaining());
    }

    @Override
    public void get(ByteBuffer dst, int dstOffset, int length) throws BufferUnderflowException {
      get(0, dst, dstOffset, length);
    }

    @Override
    public void get(long srcOffset, ByteBuffer dstBuffer, int dstOffset, int length)
        throws BufferUnderflowException {
      if (srcOffset + length > accessibleMemSize) {
        throw new BufferUnderflowException(
            "dst' length " + length + " is larger than the buffer size "
                + accessibleMemSize + ", offset: " + srcOffset);
      }

      try {
        if (dstBuffer.isDirect()) {
          ByteBuffer srcBuffer = raf.getChannel()
              .map(MapMode.READ_ONLY, accessibleMemAddress + srcOffset,
                  length);
          DirectAlignedBufferAllocator.copyMemory(((DirectBuffer) srcBuffer).address(),
              ((DirectBuffer) dstBuffer).address() + dstOffset, length);
        } else {
          Validate.isTrue(dstBuffer.hasArray());
          get(srcOffset, dstBuffer.array(), dstBuffer.arrayOffset() + dstOffset, length);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void put(byte[] src) throws BufferOverflowException {
      put(src, 0, src.length);
    }

    @Override
    public void put(byte[] src, int offset, int length) throws BufferOverflowException {
      put(0, src, offset, length);
    }

    @Override
    public void put(long dstOffset, byte[] src, int srcOffset, int length)
        throws BufferOverflowException {
      if (dstOffset + length > accessibleMemSize) {
        throw new BufferOverflowException(
            "src' length " + length + " is larger than the buffer size "
                + this.accessibleMemSize + ", dstOffset: " + dstOffset);
      }

      synchronized (raf) {
        try {
          raf.seek(accessibleMemAddress + dstOffset);
          raf.write(src, srcOffset, length);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

    @Override
    public void put(ByteBuffer src) throws BufferOverflowException {
      put(src, src.position(), src.remaining());
    }

    @Override
    public void put(ByteBuffer src, int srcOffset, int length) throws BufferOverflowException {
      put(0, src, srcOffset, length);
    }

    @Override
    public void put(long dstOffset, ByteBuffer srcBuffer, int srcOffset, int length)
        throws BufferOverflowException {
      if (dstOffset + length > this.accessibleMemSize) {
        throw new BufferOverflowException(
            "src' length " + length + " is larger than the buffer size "
                + this.accessibleMemSize + ", dstOffset: " + dstOffset);
      }

      try {
        if (srcBuffer.isDirect()) {
          ByteBuffer dstBuffer = raf.getChannel()
              .map(MapMode.READ_WRITE, accessibleMemAddress + dstOffset,
                  length);
          DirectAlignedBufferAllocator.copyMemory(((DirectBuffer) srcBuffer).address() + srcOffset,
              ((DirectBuffer) dstBuffer).address(), length);
        } else {
          Validate.isTrue(srcBuffer.hasArray());
          put(dstOffset, srcBuffer.array(), srcBuffer.arrayOffset() + srcOffset, length);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public long size() {
      return this.accessibleMemSize;
    }

    @Override
    public byte[] array() {
      if (size() == 0) {
        return null;
      }

      try {
        byte[] temp = new byte[(int) size()];
        get(temp);
        return temp;
      } catch (BufferUnderflowException e) {
        logger.error("can't clone an array", e);
        return null;
      }
    }
  }
}
