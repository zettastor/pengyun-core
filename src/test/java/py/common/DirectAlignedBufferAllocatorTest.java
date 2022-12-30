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
