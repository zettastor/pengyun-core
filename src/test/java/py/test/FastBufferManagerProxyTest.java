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

import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import py.common.FastBuffer;
import py.common.FastBufferManager;
import py.exception.NoAvailableBufferException;

public class FastBufferManagerProxyTest extends TestBase {
  @Override
  @Before
  public void init() throws Exception {
    super.init();
  }

  @Test
  public void allocateBufferTimeout() throws Exception {
    long startTime = System.currentTimeMillis();

    FastBufferManagerDirectImplForTest bufferManager = new FastBufferManagerDirectImplForTest(1024);
    try {
      bufferManager.allocateBuffer(1025, 5000);
      fail();
    } catch (NoAvailableBufferException e) {
      logger.error("caught exception", e);
    }

    long endTime = System.currentTimeMillis();

    Assert.assertTrue((endTime - startTime) >= 5000);
  }

  @Test
  public void allocateBufferAfterRelease() throws Exception {
    final FastBufferManagerDirectImplForTest bufferManager = new FastBufferManagerDirectImplForTest(
        1024);
    final FastBuffer allocatedBuffer = bufferManager.allocateBuffer(1024);
    final AtomicBoolean failed = new AtomicBoolean(false);

    Thread allocationThread = new Thread() {
      @Override
      public void run() {
        FastBuffer buffer = null;
        try {
          buffer = bufferManager.allocateBuffer(1024, 5000);
          logger.info("allocate buffer successfully");
        } catch (NoAvailableBufferException e) {
          failed.set(true);
          return;
        }
        bufferManager.releaseBuffer(buffer);
      }
    };

    allocationThread.start();
    Thread.sleep(2000);
    bufferManager.releaseBuffer(allocatedBuffer);

    Thread.sleep(5000);
    Assert.assertFalse(failed.get());
  }

  @Test
  public void timeoutAllocatingBufferAfterRelease() throws Exception {
    final FastBufferManagerDirectImplForTest bufferManager = new FastBufferManagerDirectImplForTest(
        1024);
    final FastBuffer allocatedBuffer1 = bufferManager.allocateBuffer(512, 0);
    final FastBuffer allocatedBuffer2 = bufferManager.allocateBuffer(512, 0);
    final AtomicBoolean failed = new AtomicBoolean(false);

    Thread allocationThread = new Thread() {
      @Override
      public void run() {
        try {
          bufferManager.allocateBuffer(1024, 5000);
          failed.set(true);
        } catch (NoAvailableBufferException e) {
          logger.error("caught exception", e);
        }
      }
    };

    allocationThread.start();
    Thread.sleep(2000);
    bufferManager.releaseBuffer(allocatedBuffer1);

    Thread.sleep(5000);
    Assert.assertFalse(failed.get());
    bufferManager.releaseBuffer(allocatedBuffer2);
  }
}
