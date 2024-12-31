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

import java.util.List;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Ignore;
import py.common.FastBuffer;
import py.common.FastBufferManager;
import py.common.FastBufferManagerProxy;
import py.common.HeapMemoryFastBufferImpl;
import py.exception.NoAvailableBufferException;

@Ignore
public class FastBufferManagerDirectImplForTest extends FastBufferManagerProxy {
  static {
    FastBufferManagerProxy.setSectorBits(0);
  }

  private long allocatedSize = 0L;

  public FastBufferManagerDirectImplForTest() {
    this(Integer.MAX_VALUE);
  }

  public FastBufferManagerDirectImplForTest(long size) {
    super(new FastBufferManagerForTest(size), "111");
  }

  public FastBufferManagerDirectImplForTest(FastBufferManager fastBufferManager) {
    super(fastBufferManager, "111");
  }

  public FastBuffer allocateBuffer(long size, long timeoutMs) throws NoAvailableBufferException {
    FastBuffer fastBuffer = super.allocateBuffer(size, timeoutMs);
    allocatedSize += size;
    return fastBuffer;
  }

  @Override
  public synchronized void releaseBuffer(FastBuffer retbuf) {
    long size = retbuf.size();
    super.releaseBuffer(retbuf);
    allocatedSize -= size;
  }

  public synchronized long getAllocatedSize() {
    return allocatedSize;
  }

  static class FastBufferManagerForTest implements FastBufferManager {
    private long capacity;
    private long allocatedSize = 0L;

    public FastBufferManagerForTest(long capacity) {
      this.capacity = capacity;
    }

    @Override
    public synchronized FastBuffer allocateBuffer(long size) throws NoAvailableBufferException {
      if (size + allocatedSize > capacity) {
        throw new NoAvailableBufferException(
            "capacity: " + capacity + ", allocatedSize: " + allocatedSize
                + ", size: " + size);
      }
      allocatedSize += size;
      return new HeapMemoryFastBufferImpl(new byte[(int) size]).allocate((int) size);
    }

    @Override
    public synchronized void releaseBuffer(FastBuffer retbuf) {
      allocatedSize -= retbuf.size();
    }

    @Override
    public void close() {
      allocatedSize = 0L;
    }

    @Override
    public long size() {
      return capacity;
    }

    @Override
    public List<FastBuffer> allocateBuffers(long size) throws NoAvailableBufferException {
      throw new NotImplementedException("this is a fast buffer for test");
    }

    @Override
    public long getAlignmentSize() {
      throw new NotImplementedException("this is a fast buffer for test");
    }
  }
}
