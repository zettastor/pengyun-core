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

import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import org.junit.Test;
import py.test.TestBase;

public class DirectAlignedBufferAllocatorTest extends TestBase {
  @Test
  public void testCopyHeapBufferToDirectBuffer() {
    ByteBuffer buffer = ByteBuffer.allocateDirect(4);
    ByteBuffer buffer1 = ByteBuffer.allocate(4);
    buffer1.putInt(5);

    buffer1.position(0);
    DirectAlignedBufferAllocator
        .copyMemory(buffer1.array(), buffer1.arrayOffset() + buffer1.position(),
            4, DirectAlignedBufferAllocator.getAddress(buffer));
    buffer1.position(0);

    int dest = buffer.getInt();
    int src = buffer1.getInt();
    logger.warn("read src {} dest {}", src, dest);
    assertTrue(src == dest);
    assertTrue(src == 5);
  }

  @Test
  public void testCopyReadonlyHeapBufferToDirectBuffer() {
    ByteBuffer buffer1 = ByteBuffer.allocate(4);
    buffer1.putInt(5);
    buffer1 = buffer1.asReadOnlyBuffer();

    buffer1.position(0);
    ByteBuffer buffer = ByteBuffer.allocateDirect(4);
    DirectAlignedBufferAllocator
        .copyMemory(buffer1, 4, DirectAlignedBufferAllocator.getAddress(buffer));
    buffer1.position(0);

    int dest = buffer.getInt();
    int src = buffer1.getInt();
    logger.warn("read src {} dest {}", src, dest);
    assertTrue(src == dest);
    assertTrue(src == 5);
  }
}
