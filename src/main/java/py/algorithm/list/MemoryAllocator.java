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

package py.algorithm.list;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import org.apache.commons.lang.Validate;
import py.algorithm.SimpleBitmap;

public class MemoryAllocator {
  private static final int INVALID_INDEX = -1;
  private final int[] prevs;

  private final SimpleBitmap positionTakenBitMap;
  private final int[] nexts;
  private final int nodeInfoSize;

  private final ByteBuffer nodeInfo;
  private final int capacity;
  private final ArrayDeque<Integer> releasedSpace = new ArrayDeque<>();
  private int searchPos = 0;

  public MemoryAllocator(int capacity, int nodeInfoSize) {
    this.capacity = capacity;

    this.nodeInfoSize = nodeInfoSize;
    this.prevs = new int[capacity];
    this.positionTakenBitMap = new SimpleBitmap(capacity);
    this.nexts = new int[capacity];

    this.nodeInfo = ByteBuffer.allocate(capacity * nodeInfoSize);
    Arrays.fill(this.prevs, INVALID_INDEX);
    Arrays.fill(this.nexts, INVALID_INDEX);
  }

  public int allocate() {
    int pos = searchFreePos();
    positionTakenBitMap.set(pos);

    return pos;
  }

  int getPrev(int index) {
    int prev = prevs[index];
    Validate.isTrue(prev != INVALID_INDEX);

    return prev;
  }

  int getNext(int index) {
    int next = nexts[index];
    Validate.isTrue(next != INVALID_INDEX);

    return next;
  }

  public void updateRelationship(int left, int right) {
    nexts[left] = right;
    prevs[right] = left;
  }

  public void setValue(int index, ByteBuffer buffer) {
    validateIndex(index);

    validateBufferSize(buffer.remaining());
    ByteBuffer inPutBuffer = findBuffer(index);
    inPutBuffer.put(buffer);
    buffer.clear();
  }

  public void getNode(int index, ByteBuffer buffer) {
    validateIndex(index);

    validateBufferSize(buffer.remaining());
    ByteBuffer inPutBuffer = findBuffer(index);
    buffer.put(inPutBuffer);
    buffer.clear();
  }

  private ByteBuffer findBuffer(int index) {
    ByteBuffer inPutBuffer = nodeInfo.duplicate();
    inPutBuffer.position(index * nodeInfoSize);
    inPutBuffer.limit((index + 1) * nodeInfoSize);
    validateBufferSize(inPutBuffer.remaining());
    return inPutBuffer;
  }

  public void release(int index) {
    validateIndex(index);

    prevs[index] = INVALID_INDEX;
    positionTakenBitMap.clear(index);
    nexts[index] = INVALID_INDEX;
    releasedSpace.offerLast(index);

  }

  private int searchFreePos() {
    Integer releasedPos = releasedSpace.pollFirst();
    if (releasedPos != null) {
      return releasedPos;
    }

    for (; searchPos < capacity; ++searchPos) {
      if (!positionTakenBitMap.get(searchPos)) {
        return searchPos++;
      }
    }

    throw new OutOfMemoryError(
        String.format("class:[%s] run out of memory", MemoryAllocator.class.getName()));
  }

  private void validateIndex(int index) {
    Validate.isTrue(positionTakenBitMap.get(index));
  }

  private void validateBufferSize(int buffetLength) {
    Validate.isTrue(buffetLength <= nodeInfoSize);
  }

}
