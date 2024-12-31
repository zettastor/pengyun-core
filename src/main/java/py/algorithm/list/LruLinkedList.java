/**
* Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
