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
