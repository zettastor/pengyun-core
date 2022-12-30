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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.jboss.netty.buffer.ChannelBuffer;
import org.junit.Test;
import py.common.rpc.share.PyDynamicBuffer;

public class PyDynamicBufferTest extends TestBase {
  public PyDynamicBufferTest() throws Exception {
    super.init();
  }

  @Test
  public void testWrite() {
    PyDynamicBuffer buffer = new PyDynamicBuffer(1025);
    assertTrue(buffer.getBuffer().capacity() == 1024);

    try {
      buffer = new PyDynamicBuffer(1024 * 1024 + 1);
    } catch (Exception e) {
      logger.info("caught an exception", e);
    }

    buffer = new PyDynamicBuffer();
    List<byte[]> bufferLists = new ArrayList<byte[]>();
    int bufferCount = 100;
    Random random = new Random();
    for (int i = 0; i < bufferCount; i++) {
      byte[] tmp = new byte[random.nextInt(1024)];
      for (int j = 0; j < tmp.length; j++) {
        tmp[j] = (byte) i;
      }
      bufferLists.add(tmp);
    }

    for (byte[] tmp : bufferLists) {
      buffer.write(tmp, 0, tmp.length);
    }

    ChannelBuffer result = buffer.getBuffer();
    for (int i = 0; i < bufferCount; i++) {
      ChannelBuffer tmp = result.readBytes(bufferLists.get(i).length);
      for (int j = 0; j < tmp.capacity(); j++) {
        assertTrue(tmp.getByte(j) == i);
      }
    }

    buffer = new PyDynamicBuffer();
    int capacity = 1024 * 1024 * 5;
    byte[] src = new byte[capacity];
    for (int i = 0; i < src.length; i++) {
      src[i] = 2;
    }

    buffer.write(src, 0, src.length);
    result = buffer.getBuffer().readBytes(src.length);
    for (int i = 0; i < result.capacity(); i++) {
      assertTrue(result.getByte(i) == 2);
    }
  }
}
