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