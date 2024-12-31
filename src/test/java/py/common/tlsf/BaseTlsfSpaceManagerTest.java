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

package py.common.tlsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.test.TestBase;

public class BaseTlsfSpaceManagerTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(BaseTlsfSpaceManagerTest.class);

  public void init() throws Exception {
    super.init();
  }

  private long findPossibleLagestAllocationSize(BaseTlsfSpaceManager spaceManager, long totalSize)
      throws Exception {
    long lo;
    long hi;
    long mid;

    lo = 0;
    hi = totalSize;
    while (lo + 1 < hi) {
      mid = (lo + hi) / 2;
      try {
        long addr;

        addr = spaceManager.allocate(mid);
        spaceManager.release(addr);

        lo = mid;
      } catch (OutOfSpaceException e) {
        hi = mid;
      }
    }

    return lo;
  }

  @Test
  public void testAllocateMostNumOfDivisions() throws Exception {
    final long totalSize = 1024L * 1024;

    BaseTlsfSpaceManager spaceManager = new BaseTlsfSpaceManager(new SimpleTlsfMetadata(),
        new SimpleTlsfSpaceDivisionMetadata(), 4, 0, totalSize);

    int[] naddrs = new int[2];

    for (int index : new int[]{0, 1}) {
      Set<Long> spaceDivisions = new HashSet<>();
      while (true) {
        try {
          spaceDivisions.add(spaceManager.allocate(1L));
        } catch (OutOfSpaceException e) {
          logger.debug("Allocated {} divisions each of which has at least {} bytes",
              spaceDivisions.size(),
              1L);
          break;
        }
      }

      for (long spaceDivision : spaceDivisions) {
        spaceManager.release(spaceDivision);
      }

      naddrs[index] = spaceDivisions.size();
      spaceDivisions.clear();
    }

    Assert.assertEquals(naddrs[0], naddrs[1]);
  }

  @Test
  public void testTryAllocate() throws Exception {
    final long totalSize = 1024L * 1024;

    BaseTlsfSpaceManager spaceManager = new BaseTlsfSpaceManager(new SimpleTlsfMetadata(),
        new SimpleTlsfSpaceDivisionMetadata(), 4, 0, totalSize);

    List<Long> spaceDivisions = new ArrayList<>();
    while (true) {
      try {
        spaceDivisions.add(spaceManager.allocate(1L));
      } catch (OutOfSpaceException e) {
        logger.debug("Allocated {} divisions each of which has at least {} bytes",
            spaceDivisions.size(), 1L);
        break;
      }
    }

    long desiredSize = 0;
    Set<Long> remainingSpaceDivisions = new HashSet<>();
    for (int i = 0; i < spaceDivisions.size(); i++) {
      long spaceDivision = spaceDivisions.get(i);
      if (i % 2 == 0) {
        spaceManager.release(spaceDivision);
        desiredSize += spaceManager.getDivisionMetadata().getAccessibleMemSize(spaceDivision);
      } else {
        remainingSpaceDivisions.add(spaceDivision);
      }
    }

    try {
      spaceManager.allocate(desiredSize);
      Assert.fail();
    } catch (OutOfSpaceException e) {
      logger.error("caught exception", e);
    }

    while (desiredSize > 0) {
      long spaceDivision;
      try {
        spaceDivision = spaceManager.allocate(desiredSize);
      } catch (OutOfSpaceException e) {
        spaceDivision = spaceManager.tryAllocate(desiredSize);
      }

      remainingSpaceDivisions.add(spaceDivision);
      desiredSize -= spaceManager.getDivisionMetadata().getAccessibleMemSize(spaceDivision);
    }
  }

  @Test
  public void testExtendBehind() throws Exception {
    final long totalSize = 1024L * 1024 * 1024 * 1024 * 1024;
    final int times = 10;
    long maxSize;
    List<Long> addrs = new ArrayList<>();

    BaseTlsfSpaceManager spaceManager = new BaseTlsfSpaceManager(new SimpleTlsfMetadata(),
        new SimpleTlsfSpaceDivisionMetadata(), 4, 0, totalSize);

    maxSize = findPossibleLagestAllocationSize(spaceManager, totalSize);
    addrs.add(spaceManager.allocate(maxSize));
    try {
      spaceManager.allocate(maxSize);
      Assert.fail();
    } catch (OutOfSpaceException e) {
      logger.error("caught exception", e);
    }
    for (int i = 0; i < times; i++) {
      logger.debug("This is {} time to extend space!", i + 1);
      spaceManager.extend((i + 1) * totalSize, totalSize);

      addrs.add(spaceManager.allocate(maxSize));
      try {
        spaceManager.allocate(maxSize);
        Assert.fail();
      } catch (OutOfSpaceException e) {
        logger.error("caught exception", e);
      }
    }

    for (long addr : addrs) {
      spaceManager.release(addr);
    }
  }

  @Test
  public void testExtendBefore() throws Exception {
    final long totalSize = 1024L * 1024 * 1024 * 1024 * 1024;
    final int times = 10;
    long maxSize;
    List<Long> addrs = new ArrayList<>();

    BaseTlsfSpaceManager spaceManager = new BaseTlsfSpaceManager(new SimpleTlsfMetadata(),
        new SimpleTlsfSpaceDivisionMetadata(), 4, times * totalSize, totalSize);

    maxSize = findPossibleLagestAllocationSize(spaceManager, totalSize);
    addrs.add(spaceManager.allocate(maxSize));
    try {
      spaceManager.allocate(maxSize);
      Assert.fail();
    } catch (OutOfSpaceException e) {
      logger.error("caught exception", e);
    }
    for (int i = 0; i < times; i++) {
      logger.debug("This is {} time to extend space!", i + 1);
      spaceManager.extend((times - i - 1) * totalSize, totalSize);

      addrs.add(spaceManager.allocate(maxSize));
      try {
        spaceManager.allocate(maxSize);
        Assert.fail();
      } catch (OutOfSpaceException e) {
        logger.error("caught exception", e);
      }
    }

    for (long addr : addrs) {
      spaceManager.release(addr);
    }
  }

  @Test
  public void testAllocateConcurrently() throws Exception {
    final long totalSize = 1024L * 1024 * 1024 * 1024 * 1024;
    final BaseTlsfSpaceManager spaceManager = new BaseTlsfSpaceManager(new SimpleTlsfMetadata(),
        new SimpleTlsfSpaceDivisionMetadata(), 4, 0, totalSize);

    final int nAllocatingThreads = 100;
    final AtomicBoolean failed = new AtomicBoolean(false);
    final CountDownLatch threadLatch = new CountDownLatch(nAllocatingThreads);
    final CountDownLatch mainLatch = new CountDownLatch(1);
    final Random desiredSizeGenerator = new Random(System.currentTimeMillis());
    long maxSize;

    maxSize = findPossibleLagestAllocationSize(spaceManager, totalSize);
    for (int i = 0; i < nAllocatingThreads; i++) {
      new Thread("Allocation " + i) {
        Random allocateOrRelease = new Random();
        List<Long> addressList = new ArrayList<>();

        public void run() {
          try {
            mainLatch.await();
          } catch (InterruptedException e) {
            logger.error("caught exception", e);
          }

          try {
            for (int i = 0; i < 100; i++) {
              if (allocateOrRelease.nextBoolean()) {
                long desiredSize = desiredSizeGenerator.nextInt(1024 * 1024 * 1024);
                if (desiredSize == 0) {
                  desiredSize = 1L;
                }

                try {
                  addressList.add(spaceManager.allocate(desiredSize));
                } catch (OutOfSpaceException e) {
                  logger.debug("Caught an out of space exception!");
                  continue;
                }
              } else {
                if (addressList.isEmpty()) {
                  continue;
                }

                Collections.shuffle(addressList);
                long address = addressList.remove(addressList.size() - 1);
                spaceManager.release(address);
              }
            }

            for (long address : addressList) {
              spaceManager.release(address);
            }

          } catch (Exception e) {
            logger.error("Caught an exception", e);
            failed.set(true);
          } finally {
            threadLatch.countDown();
          }
        }
      }.start();
    }

    mainLatch.countDown();
    threadLatch.await();

    Assert.assertFalse(failed.get());

    long address = spaceManager.allocate(maxSize);
    spaceManager.release(address);
  }
}
