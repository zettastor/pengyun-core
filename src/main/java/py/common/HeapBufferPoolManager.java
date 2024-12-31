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

package py.common;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeapBufferPoolManager {
  private static final Logger logger = LoggerFactory.getLogger(HeapBufferPoolManager.class);
  private static final HeapBufferPoolManager bufferPoolManager = new HeapBufferPoolManager();
  private final BlockingQueue<ByteBuffer> bufferQueue;

  private HeapBufferPoolManager() {
    this.bufferQueue = new LinkedBlockingQueue<ByteBuffer>();
  }

  public static HeapBufferPoolManager getInstance() {
    return bufferPoolManager;
  }

  public void init(int pageCount, int pageSize) {
    for (int i = 0; i < pageCount; i++) {
      release(ByteBuffer.wrap(new byte[pageSize]));
    }

    logger.warn("heap pool buffer: {}, pageSize: {}", bufferQueue.size(), pageSize);
  }

  public ByteBuffer allocate() {
    ByteBuffer byteBuffer = bufferQueue.poll();
    return byteBuffer;
  }

  public void release(ByteBuffer byteBuffer) {
    if (!bufferQueue.offer(byteBuffer)) {
      logger.warn(
          "oh, my god, i can add the page back, i will lost a page, but the total count is limited,"
              + " so you can imagine the result");
    }
  }

  public int size() {
    return bufferQueue.size();
  }
}
