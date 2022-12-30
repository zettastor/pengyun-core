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
