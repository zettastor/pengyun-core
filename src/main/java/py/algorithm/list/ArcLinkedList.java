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
