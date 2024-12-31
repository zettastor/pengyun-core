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

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.exception.NoAvailableBufferException;

public class SyncLogTlsfFastBufferManager extends AbstractTlsfastBufferManager {
  private static final Logger logger = LoggerFactory.getLogger(SyncLogTlsfFastBufferManager.class);

  public SyncLogTlsfFastBufferManager(long totalSize) {
    super(new TlsfFastBufferManager(totalSize, SyncLogTlsfFastBufferManager.class.getSimpleName()));
  }

  public SyncLogTlsfFastBufferManager(int alignmentSize, long totalSize) {
    super(new TlsfFastBufferManager(alignmentSize, totalSize,
        SyncLogTlsfFastBufferManager.class.getSimpleName()));
  }

  @Override
  public FastBuffer allocateBuffer(long size) throws NoAvailableBufferException {
    if (size <= 0) {
      logger.error("Invalid size {}, the requested size to allocate must be positive", size);
      return null;
    }
    return new SyncLogFastBufferImpl(super.allocateBuffers(size), size);
  }

  @Override
  public void releaseBuffer(FastBuffer retBuf) {
    Validate.isTrue(retBuf instanceof SyncLogFastBufferImpl);
    super.releaseBuffer(retBuf);
  }

}

