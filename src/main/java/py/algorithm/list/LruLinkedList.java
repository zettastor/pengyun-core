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

public class LruLinkedList {
  private final MemoryAllocator memoryAllocator;
  private final ArrayLinkedListBase origin;

  private final int capacity;

  public LruLinkedList(int capacity, int nodeInfoSize) {
    this.capacity = capacity;
    this.memoryAllocator = new MemoryAllocator(capacity + ArrayLinkedListBase.EXTRA_NODE_COUNT,
        nodeInfoSize);
    this.origin = new ArrayLinkedListBase(memoryAllocator);
  }

  public void getNode(int index, ByteBuffer buffer) {
    this.memoryAllocator.getNode(index, buffer);
  }

  public int addMru(ByteBuffer buffer) {
    return origin.addFirst(buffer);
  }

  public int addlru(ByteBuffer buffer) {
    return origin.addLast(buffer);
  }

  public int removeLru(ByteBuffer buffer) {
    return origin.removeLast(buffer);
  }

  public void remove(int arrayIndex) {
    origin.remove(arrayIndex);
  }

  public int size() {
    return origin.size();
  }

  public boolean empty() {
    return origin.size() <= 0;
  }

  public boolean full() {
    return origin.size() >= capacity;
  }

  public int getMru(ByteBuffer buffer) {
    return origin.getFirst(buffer);
  }
}
