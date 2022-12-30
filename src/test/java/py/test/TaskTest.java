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

package py.test;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import py.common.DirectAlignedBufferAllocator;
import py.storage.PriorityStorage;
import py.storage.impl.Task;

public class TaskTest extends TestBase {
  private static final int MAX_BUFFER_POLL_SIZE = 128;
  private static final int MAX_MEGER_COUNT = 64;
  private final BlockingQueue<ByteBuffer> mergeBufferPool = new LinkedBlockingQueue<>(
      MAX_BUFFER_POLL_SIZE);
  private final int pagePhysicalSize = 8704;

  @Before
  public void before() {
    for (int i = 0; i < MAX_BUFFER_POLL_SIZE / 2; i++) {
      ByteBuffer buffer = DirectAlignedBufferAllocator
          .allocateAlignedByteBuffer(pagePhysicalSize * MAX_MEGER_COUNT);
      mergeBufferPool.offer(buffer);
    }
  }

  @After
  public void after() {
  }

  @Test
  public void testMergeRead() throws InterruptedException {
    int count = 100;
    LinkedList<Task> taskList = new LinkedList<>();
    CountDownLatch latch = new CountDownLatch(count);

    int step = 10;
    for (int i = 0; i < count; i++) {
      taskList.add(new Task<>(Task.TaskType.Read, PriorityStorage.Priority.MIDDLE, i * step, null,
          ByteBuffer.allocate(step),
          new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
              latch.countDown();
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
              latch.countDown();
            }
          }));
    }

    Task task = Task.merge(taskList, mergeBufferPool, 8704 * 32);
    task.buffer.position(count * step);
    task.complete(count * step);

    latch.await();
  }

}