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

public class ArrayLinkedListBase {
  public static final int EXTRA_NODE_COUNT = 2;

  public static final int INVALID_ARRAY_INDEX = -1;
  private final int head;
  private final int tail;
  private final MemoryAllocator memoryAllocator;
  private int size;

  public ArrayLinkedListBase(MemoryAllocator memoryAllocator) {
    this.memoryAllocator = memoryAllocator;

    this.head = memoryAllocator.allocate();
    this.tail = memoryAllocator.allocate();

    memoryAllocator.updateRelationship(head, tail);
    memoryAllocator.updateRelationship(tail, head);
  }

  public int addLast(ByteBuffer buffer) {
    int left = memoryAllocator.getPrev(tail);
    int index = insert(buffer, left, tail);
    return index;
  }

  public int addFirst(ByteBuffer buffer) {
    int right = memoryAllocator.getNext(head);
    int index = insert(buffer, head, right);
    return index;
  }

  public int removeFirst(ByteBuffer buffer) {
    if (empty()) {
      return INVALID_ARRAY_INDEX;
    }

    int index = memoryAllocator.getNext(head);
    memoryAllocator.getNode(index, buffer);
    remove(index);

    return index;
  }

  public int removeLast(ByteBuffer buffer) {
    if (empty()) {
      return INVALID_ARRAY_INDEX;
    }

    int index = memoryAllocator.getPrev(tail);
    memoryAllocator.getNode(index, buffer);
    remove(index);

    return index;
  }

  public int getFirst(ByteBuffer buffer) {
    if (empty()) {
      return INVALID_ARRAY_INDEX;
    }

    int index = memoryAllocator.getNext(head);
    memoryAllocator.getNode(index, buffer);
    return index;
  }

  public int getLast(ByteBuffer buffer) {
    if (empty()) {
      return INVALID_ARRAY_INDEX;
    }

    int index = memoryAllocator.getPrev(tail);
    memoryAllocator.getNode(index, buffer);
    return index;
  }

  private int insert(ByteBuffer buffer, int left, int right) {
    int index = memoryAllocator.allocate();
    memoryAllocator.setValue(index, buffer);

    memoryAllocator.updateRelationship(left, index);
    memoryAllocator.updateRelationship(index, right);

    ++size;

    return index;
  }

  public void remove(int index) {
    int left = memoryAllocator.getPrev(index);
    int right = memoryAllocator.getNext(index);

    memoryAllocator.release(index);
    memoryAllocator.updateRelationship(left, right);

    --size;
  }

  public int size() {
    return size;
  }

  public boolean empty() {
    return size <= 0;
  }

}
