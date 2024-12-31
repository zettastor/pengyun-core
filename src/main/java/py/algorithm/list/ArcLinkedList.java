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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.Validate;
import py.algorithm.QueueType;

public class ArcLinkedList {
  private final QueueType queueType;
  private final ArrayLinkedListBase origin;

  private final int bufferSize;
  private final LinkedBlockingQueue<ByteBuffer> bufferPoll = new LinkedBlockingQueue<>();

  public ArcLinkedList(QueueType queueType, MemoryAllocator memoryAllocator, int bufferSize) {
    this.queueType = queueType;
    this.origin = new ArrayLinkedListBase(memoryAllocator);
    this.bufferSize = bufferSize;
    for (int i = 0; i < 1000; i++) {
      ByteBuffer buffer = ByteBuffer.allocate(bufferSize + 1);
      bufferPoll.add(buffer);
    }
  }

  public int addMru(ByteBuffer buffer) {
    validateBuffer(buffer.remaining());
    ByteBuffer tmpBuffer = generateBuffer(buffer);
    int index = origin.addFirst(tmpBuffer);
    bufferPoll.offer(tmpBuffer);
    return index;
  }

  public int addLru(ByteBuffer buffer) {
    validateBuffer(buffer.remaining());
    ByteBuffer tmpBuffer = generateBuffer(buffer);
    int index = origin.addLast(tmpBuffer);
    bufferPoll.offer(tmpBuffer);
    return index;
  }

  public int removeMru(ByteBuffer buffer) {
    validateBuffer(buffer.remaining());
    ByteBuffer tmpBuffer = allocateBuffer();
    final int index = origin.removeFirst(tmpBuffer);
    tmpBuffer.clear();
    tmpBuffer.limit(bufferSize);
    buffer.put(tmpBuffer);
    buffer.clear();
    releaseBuffer(tmpBuffer);
    return index;
  }

  public int removeLru(ByteBuffer buffer) {
    validateBuffer(buffer.remaining());
    ByteBuffer tmpBuffer = allocateBuffer();
    final int index = origin.removeLast(tmpBuffer);
    tmpBuffer.clear();
    tmpBuffer.limit(bufferSize);
    buffer.put(tmpBuffer);
    buffer.clear();
    releaseBuffer(tmpBuffer);
    return index;
  }

  private ByteBuffer generateBuffer(ByteBuffer buffer) {
    ByteBuffer tmpBuffer = allocateBuffer();
    tmpBuffer.put(buffer.duplicate());
    tmpBuffer.put((byte) queueType.index());
    tmpBuffer.clear();
    return tmpBuffer;
  }

  public void remove(int arrayIndex) {
    origin.remove(arrayIndex);
  }

  public QueueType getQueueType() {
    return queueType;
  }

  public int size() {
    return origin.size();
  }

  public int getLru(ByteBuffer buffer) {
    ByteBuffer tmpBuffer = allocateBuffer();
    tmpBuffer.clear();
    final int index = origin.getLast(tmpBuffer);
    tmpBuffer.limit(bufferSize);
    buffer.put(tmpBuffer);
    buffer.clear();
    releaseBuffer(tmpBuffer);
    return index;
  }

  public int getMru(ByteBuffer buffer) {
    return origin.getFirst(buffer);
  }

  private ByteBuffer allocateBuffer() {
    try {
      ByteBuffer byteBuffer = bufferPoll.poll(1, TimeUnit.DAYS);
      byteBuffer.clear();
      return byteBuffer;
    } catch (InterruptedException e) {
      return null;
    }
  }

  private void releaseBuffer(ByteBuffer buffer) {
    bufferPoll.offer(buffer);
  }

  private void validateBuffer(int inputBufferSize) {
    Validate.isTrue(inputBufferSize <= bufferSize);
  }
}
