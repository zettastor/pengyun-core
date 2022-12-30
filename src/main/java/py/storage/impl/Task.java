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

package py.storage.impl;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.DirectAlignedBufferAllocator;
import py.storage.PriorityStorage;

public class Task<A> implements Comparable<Task> {
  private static final Logger logger = LoggerFactory.getLogger(Task.class);

  private static final AtomicLong seq = new AtomicLong(0);
  public final ByteBuffer buffer;
  final Task.TaskType type;
  final PriorityStorage.Priority priority;
  final long pos;
  final A attachment;
  private final long seqNum;
  private final CompletionHandler<Integer, ? super A> handler;

  public Task(TaskType type, PriorityStorage.Priority priority, long pos, A attachment,
      ByteBuffer buffer,
      CompletionHandler<Integer, ? super A> handler) {
    this.pos = pos;
    this.attachment = attachment;
    this.buffer = buffer;
    this.handler = handler;
    this.seqNum = seq.incrementAndGet();
    this.type = type;
    this.priority = priority;
  }

  public static Task merge(LinkedList<Task> canBeMergedTasks,
      BlockingQueue<ByteBuffer> blockingQueuePool, int poolSzie) {
    if (canBeMergedTasks == null || canBeMergedTasks.size() == 0) {
      throw new IllegalArgumentException("empty tasks " + canBeMergedTasks);
    }

    if (canBeMergedTasks.size() == 1) {
      return canBeMergedTasks.get(0);
    }

    int length = 0;
    for (Task canBeMergedTask : canBeMergedTasks) {
      length += canBeMergedTask.buffer.remaining();
    }

    ByteBuffer poolBuffer;
    if (length > poolSzie) {
      poolBuffer = DirectAlignedBufferAllocator.allocateAlignedByteBuffer(length);
    } else {
      poolBuffer = blockingQueuePool.poll();
    }

    if (poolBuffer == null) {
      logger.info("blocking queue pool is empty. allocate new byte buffer, length:{}", length);
      poolBuffer = DirectAlignedBufferAllocator
          .allocateAlignedByteBuffer(length > poolSzie ? length : poolSzie);
    }

    ByteBuffer directBuffer = poolBuffer;
    directBuffer.clear();
    directBuffer.limit(length);

    if (directBuffer.remaining() < length) {
      throw new IllegalArgumentException(
          "buffer remaining: " + directBuffer.remaining() + "length: " + length);
    }
    TaskType type = canBeMergedTasks.get(0).type;
    if (type == TaskType.Read) {
      directBuffer.limit(length);
      return new Task<>(type, canBeMergedTasks.get(0).priority, canBeMergedTasks.get(0).pos, null,
          directBuffer,
          new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
              try {
                directBuffer.flip();
                for (Task canBeMergedTask : canBeMergedTasks) {
                  int length = canBeMergedTask.buffer.remaining();
                  while (canBeMergedTask.buffer.hasRemaining()) {
                    canBeMergedTask.buffer.put(directBuffer.get());
                  }
                  canBeMergedTask.complete(length);
                }
              } catch (Throwable t) {
                logger.error("error", t);
              } finally {
                directBuffer.clear();
                if (directBuffer.remaining() == poolSzie) {
                  blockingQueuePool.offer(directBuffer);
                }
              }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
              try {
                for (Task canBeMergedTask : canBeMergedTasks) {
                  canBeMergedTask.failed(exc);
                }
              } catch (Throwable t) {
                logger.error("error", t);
              } finally {
                directBuffer.clear();
                if (directBuffer.remaining() == poolSzie) {
                  blockingQueuePool.offer(directBuffer);
                }
              }
            }
          });
    } else if (type == TaskType.Write) {
      int[] lengthArray = new int[canBeMergedTasks.size()];
      for (int i = 0; i < canBeMergedTasks.size(); i++) {
        Task canBeMergedTask = canBeMergedTasks.get(i);
        lengthArray[i] = canBeMergedTask.buffer.remaining();
        directBuffer.put(canBeMergedTask.buffer);
      }
      directBuffer.flip();
      return new Task<>(type, canBeMergedTasks.get(0).priority, canBeMergedTasks.get(0).pos, null,
          directBuffer,
          new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
              for (int i = 0; i < canBeMergedTasks.size(); i++) {
                Task canBeMergedTask = canBeMergedTasks.get(i);
                canBeMergedTask.complete(lengthArray[i]);
              }
              directBuffer.clear();
              if (directBuffer.remaining() == poolSzie) {
                blockingQueuePool.offer(directBuffer);
              }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
              for (Task canBeMergedTask : canBeMergedTasks) {
                canBeMergedTask.failed(exc);
              }
              directBuffer.clear();
              if (directBuffer.remaining() == poolSzie) {
                blockingQueuePool.offer(directBuffer);
              }
            }
          });
    } else {
      directBuffer.clear();
      if (directBuffer.remaining() == poolSzie) {
        blockingQueuePool.offer(directBuffer);
      }
      throw new IllegalArgumentException("illegal task type " + type);
    }
  }

  @Override
  public int compareTo(@Nonnull Task o) {
    int res = Integer.compare(priority.getVal(), o.priority.getVal());
    if (res == 0 && o != this) {
      res = (seqNum < o.seqNum ? -1 : 1);
    }
    return res;
  }

  int compareToWithOffset(@Nonnull Task o) {
    int res = Integer.compare(priority.getVal(), o.priority.getVal());
    if (res == 0 && o != this) {
      res = (pos < o.pos ? -1 : 1);
    }
    return res;
  }

  public void complete(Integer result) {
    handler.completed(result, attachment);
  }

  public void failed(Throwable exc) {
    handler.failed(exc, attachment);
  }

  @Override
  public String toString() {
    return "Task{" + "seqNum=" + seqNum + ", type=" + type + ", priority=" + priority + ", offset="
        + pos
        + ", attachment=" + attachment + ", buffer=" + buffer + '}';
  }

  boolean canMergeBefore(@Nonnull Task another) {
    if (type != another.type || priority != another.priority) {
      return false;
    }

    return pos + buffer.remaining() == another.pos;
  }

  public enum TaskType {
    Read,
    Write,
  }
}
