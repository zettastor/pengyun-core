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

package py.datanode.storage.cache.list;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import py.algorithm.QueueType;
import py.algorithm.list.ArcLinkedList;
import py.algorithm.list.ArrayLinkedListBase;
import py.algorithm.list.MemoryAllocator;
import py.test.TestBase;

public class ArcLinkedListTest extends TestBase {
  private static final int BufferSize = Integer.BYTES;
  private static final BiConsumer[] baseAdders = new BiConsumer[]{
      (BiConsumer<LinkedList<BucketNode>, BucketNode>) LinkedList::addFirst,
      (BiConsumer<LinkedList<BucketNode>, BucketNode>) LinkedList::addLast};
  private static final MyFunction[] targetAdders = new MyFunction[]{
      (MyFunction<ArcLinkedList, Integer>) ArcLinkedList::addMru,
      (MyFunction<ArcLinkedList, Integer>) ArcLinkedList::addLru};
  private static final Function[] baseRemovers = new Function[]{
      (Function<LinkedList<BucketNode>, BucketNode>) LinkedList::removeFirst,
      (Function<LinkedList<BucketNode>, BucketNode>) LinkedList::removeLast};
  private static final MyFunction[] targetRemovers = new MyFunction[]{
      (MyFunction<ArcLinkedList, Integer>) ArcLinkedList::removeMru,
      (MyFunction<ArcLinkedList, Integer>) ArcLinkedList::removeLru};

  private static void add(LinkedList<BucketNode> baseList, ArcLinkedList targetList,
      int bucketIndex, long compactAddr) {
    int operateIndex = RandomUtils.nextInt(baseAdders.length);
    final BiConsumer<LinkedList<BucketNode>, BucketNode> baseAdder = baseAdders[operateIndex];
    MyFunction<ArcLinkedList, Integer> targetAdder = targetAdders[operateIndex];

    ByteBuffer buffer = ByteBuffer.allocate(BufferSize);
    buffer.clear();
    buffer.putInt(bucketIndex);
    buffer.clear();

    int arrayIndex = targetAdder.apply(targetList, buffer);
    BucketNode node = new BucketNode(arrayIndex, buffer);
    baseAdder.accept(baseList, node);

    Assert.assertEquals(baseList.size(), targetList.size());
  }

  private static void remove(LinkedList<BucketNode> baseList, ArcLinkedList targetList) {
    if (baseList.isEmpty()) {
      return;
    }

    if (RandomUtils.nextInt(baseRemovers.length + 1) == 0) {
      BucketNode node = baseList.remove(RandomUtils.nextInt(baseList.size()));
      targetList.remove(node.getArrayIndex());
    } else {
      int operateIndex = RandomUtils.nextInt(baseRemovers.length);
      Function<LinkedList<BucketNode>, BucketNode> baseRemover = baseRemovers[operateIndex];
      MyFunction<ArcLinkedList, Integer> targetRemover = targetRemovers[operateIndex];

      BucketNode baseNode = baseRemover.apply(baseList);
      ByteBuffer buffer = ByteBuffer.allocate(BufferSize);
      buffer.clear();
      Integer targetNode = targetRemover.apply(targetList, buffer.duplicate());

      Assert.assertEquals(baseNode.getBuffer().getInt(), buffer.getInt());
    }
  }

  @Test
  public void addThenEmpty() {
    addThenEmpty(LinkedList::addFirst, ArcLinkedList::addMru, LinkedList::removeFirst,
        ArcLinkedList::removeMru);

    addThenEmpty(LinkedList::addFirst, ArcLinkedList::addMru, LinkedList::removeLast,
        ArcLinkedList::removeLru);

    addThenEmpty(LinkedList::addLast, ArcLinkedList::addLru, LinkedList::removeFirst,
        ArcLinkedList::removeMru);

    addThenEmpty(LinkedList::addLast, ArcLinkedList::addLru, LinkedList::removeLast,
        ArcLinkedList::removeLru);
  }

  private void addThenEmpty(BiConsumer<LinkedList<BucketNode>, BucketNode> baseAdder,
      MyFunction<ArcLinkedList, Integer> targetAdder,
      Function<LinkedList<BucketNode>, BucketNode> baseRemover,
      MyFunction<ArcLinkedList, Integer> targetRemover) {
    final int capacity = 1024;

    MemoryAllocator memoryAllocator = new MemoryAllocator(
        capacity + ArrayLinkedListBase.EXTRA_NODE_COUNT, BufferSize + Byte.BYTES);
    ArcLinkedList targetList = new ArcLinkedList(QueueType.T1, memoryAllocator, BufferSize);

    LinkedList<BucketNode> baseList = new LinkedList<>();

    for (int i = 0; i < capacity; i++) {
      ByteBuffer buffer = ByteBuffer.allocate(BufferSize);
      buffer.clear();
      buffer.putInt(i);
      buffer.clear();
      Integer index = targetAdder.apply(targetList, buffer.duplicate());
      BucketNode bucketNode = new BucketNode(index, buffer);
      baseAdder.accept(baseList, bucketNode);

      Assert.assertEquals(baseList.size(), targetList.size());
    }

    for (int i = 0; i < capacity; i++) {
      final BucketNode baseNode = baseRemover.apply(baseList);
      ByteBuffer buffer = ByteBuffer.allocate(BufferSize);
      buffer.clear();
      Integer index = targetRemover.apply(targetList, buffer);
      buffer.clear();
      Assert.assertEquals(baseList.size(), targetList.size());
      Assert.assertEquals(baseNode.getBuffer().getInt(), buffer.getInt());
    }
  }

  @Test
  public void randomAddAndRemove() {
    final int capacity = 1024;

    MemoryAllocator memoryAllocator = new MemoryAllocator(
        capacity + ArrayLinkedListBase.EXTRA_NODE_COUNT, BufferSize + Byte.BYTES);
    ArcLinkedList targetList = new ArcLinkedList(QueueType.T1, memoryAllocator, BufferSize);

    LinkedList<BucketNode> baseList = new LinkedList<>();

    int i = 0;
    for (; i < capacity / 2; i++) {
      add(baseList, targetList, i, i);
    }

    for (; i < capacity * 100; i++) {
      if (RandomUtils.nextBoolean()) {
        if (baseList.size() >= capacity) {
          continue;
        }

        add(baseList, targetList, i, i);
      } else {
        remove(baseList, targetList);
      }

      Assert.assertEquals(baseList.size(), targetList.size());
    }

    emptyAndCompare(baseList, targetList);
  }

  @Test
  public void multiList() {
    final int capacity = 1024 * 20;
    LinkedList[] baseLists = new LinkedList[]{
        new LinkedList<BucketNode>(),
        new LinkedList<BucketNode>(),
        new LinkedList<BucketNode>(),
        new LinkedList<BucketNode>()
    };

    MemoryAllocator memoryAllocator = new MemoryAllocator(
        capacity + ArrayLinkedListBase.EXTRA_NODE_COUNT * baseLists.length,
        BufferSize + Byte.BYTES);

    ArcLinkedList[] targetLists = new ArcLinkedList[]{
        new ArcLinkedList(QueueType.T1, memoryAllocator, BufferSize),
        new ArcLinkedList(QueueType.T2, memoryAllocator, BufferSize),
        new ArcLinkedList(QueueType.B1, memoryAllocator, BufferSize),
        new ArcLinkedList(QueueType.B2, memoryAllocator, BufferSize)
    };

    int totalSize = 0;

    int i = 0;
    for (; i < capacity; i++) {
      int listIndex = RandomUtils.nextInt(baseLists.length);
      LinkedList<BucketNode> baseList = baseLists[listIndex];
      ArcLinkedList targetList = targetLists[listIndex];

      add(baseList, targetList, i, i);
      ++totalSize;
    }

    for (; i < capacity * 10; i++) {
      int listIndex = RandomUtils.nextInt(baseLists.length);
      LinkedList<BucketNode> baseList = baseLists[listIndex];
      ArcLinkedList targetList = targetLists[listIndex];

      if (RandomUtils.nextBoolean()) {
        if (totalSize >= capacity) {
          continue;
        }

        add(baseList, targetList, i, i);
        ++totalSize;
      } else {
        remove(baseList, targetList);
        --totalSize;
      }
    }

    for (int j = 0; j < baseLists.length; j++) {
      LinkedList<BucketNode> baseList = baseLists[j];
      ArcLinkedList targetList = targetLists[j];

      emptyAndCompare(baseList, targetList);
    }
  }

  private void emptyAndCompare(LinkedList<BucketNode> baseList, ArcLinkedList targetList) {
    while (!baseList.isEmpty()) {
      BucketNode baseNode = baseList.removeFirst();
      ByteBuffer buffer = ByteBuffer.allocate(BufferSize);
      buffer.clear();
      targetList.removeMru(buffer);

      Assert.assertEquals(baseNode.getBuffer().getInt(), buffer.duplicate().getInt());
    }
  }

  interface MyFunction<T, R> {
    R apply(T t, ByteBuffer buffer);
  }

  private static class BucketNode {
    private int arrayIndex;
    private ByteBuffer buffer;

    public BucketNode(int arrayIndex, ByteBuffer buffer) {
      this.arrayIndex = arrayIndex;
      this.buffer = buffer;
    }

    public int getArrayIndex() {
      return arrayIndex;
    }

    public ByteBuffer getBuffer() {
      return buffer;
    }
  }
}
