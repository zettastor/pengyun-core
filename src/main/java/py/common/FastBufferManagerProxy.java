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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.exception.NoAvailableBufferException;

public class FastBufferManagerProxy {
  private static final Logger logger = LoggerFactory.getLogger(FastBufferManagerProxy.class);
  /**
   * The default minimum alignment is 512 bytes, because the read and write unit of storage is
   * aligned with 512 which equals the size of sector. We will split the total buffer by the size of
   * sector, then we can support the max size of buffer up to 2T.
   */
  private static int SECTOR_BITS = 9;
  private final FastBufferManager fastBufferManager;
  private final Semaphore semaphore;
  private int totalSectorCount;

  public FastBufferManagerProxy(FastBufferManager fastBufferManager, String devName) {
    this.fastBufferManager = fastBufferManager;
    totalSectorCount = (int) (fastBufferManager.size() >> SECTOR_BITS);
    Validate.isTrue(totalSectorCount <= Integer.MAX_VALUE,
        "count:" + totalSectorCount + ", size:" + fastBufferManager.size() + ", exceed "
            + Integer.MAX_VALUE);
    Validate.isTrue(totalSectorCount > 0,
        "totalSectorCount:" + totalSectorCount + ", manager size:" + fastBufferManager.size()
            + "sector_bits:" + SECTOR_BITS);
    logger.warn("fast buffer manager size:{}, total sector size:{}, sector_bits:{}",
        fastBufferManager.size(),
        totalSectorCount, SECTOR_BITS);

    semaphore = new Semaphore(totalSectorCount, true);
  }

  public static void setSectorBits(int sectorBits) {
    SECTOR_BITS = sectorBits;
  }

  public int getTotalSectorCount() {
    return totalSectorCount;
  }

  public int getFreeSectorCount() {
    return semaphore.availablePermits();
  }

  public FastBuffer allocateBuffer(long size) throws NoAvailableBufferException {
    return allocateBuffer(size, 0);
  }

  /**
   * Allocate buffer of specified size for at most specified time.
   *
   * <p>When allocation at this time failed with {@link NoAvailableBufferException}.
   */
  public FastBuffer allocateBuffer(long size, long timeoutMs) throws NoAvailableBufferException {
    if (size <= 0) {
      logger.error("can't assign a fast buffer with not-positive value {} ", size);
      return null;
    }

    if (size > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("size: " + size + " exceed " + Integer.MAX_VALUE);
    }

    int sectorCount = (int) (size >> SECTOR_BITS);
    if ((size - (((long) (sectorCount)) << SECTOR_BITS)) != 0) {
      throw new IllegalArgumentException("size: " + size + " is not aligned ");
    }

    boolean isSuccess;

    try {
      isSuccess = semaphore.tryAcquire(sectorCount, timeoutMs, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      logger.error("caught an interrupted exception while waiting for free buffers");
      throw new NoAvailableBufferException();
    }

    if (isSuccess) {
      try {
        isSuccess = false;
        isSuccess = true;
        FastBuffer fastBuffer = fastBufferManager.allocateBuffer(size);
        return fastBuffer;
      } finally {
        if (!isSuccess) {
          logger.debug("fail to allocate, size={}, count={}, available={}", size, sectorCount,
              semaphore.availablePermits());
          semaphore.release(sectorCount);
        }
      }
    } else {
      logger.debug("Timeout for {} ms to allocate buffer with size {}, count={}, available={}",
          timeoutMs, size,
          sectorCount, semaphore.availablePermits());
      throw new NoAvailableBufferException();
    }
  }

  public void releaseBuffer(FastBuffer retbuf) {
    long size = retbuf.size();
    fastBufferManager.releaseBuffer(retbuf);
    int allocateCount = (int) (size >> SECTOR_BITS);
    semaphore.release(allocateCount);
  }

  public void close() {
    fastBufferManager.close();
  }
}
