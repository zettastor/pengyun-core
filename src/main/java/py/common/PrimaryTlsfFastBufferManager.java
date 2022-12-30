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

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.exception.NoAvailableBufferException;

public class PrimaryTlsfFastBufferManager extends AbstractTlsfastBufferManager {
  private static final Logger logger = LoggerFactory.getLogger(PrimaryTlsfFastBufferManager.class);

  public PrimaryTlsfFastBufferManager(long totalSize) {
    super(new TlsfFastBufferManager(totalSize, SyncLogTlsfFastBufferManager.class.getSimpleName()));
  }

  public PrimaryTlsfFastBufferManager(int alignmentSize, long totalSize) {
    super(new TlsfFastBufferManager(alignmentSize, totalSize,
        PrimaryTlsfFastBufferManager.class.getSimpleName()));
  }

  @Override
  public FastBuffer allocateBuffer(long size) throws NoAvailableBufferException {
    if (size <= 0) {
      logger.error("Invalid size {}, the requested size to allocate must be positive", size);
      return null;
    }
    return new PrimaryFastBufferImpl(super.allocateBuffers(size), size);
  }

  @Override
  public void releaseBuffer(FastBuffer retBuf) {
    Validate.isTrue(retBuf instanceof PrimaryFastBufferImpl);
    super.releaseBuffer(retBuf);
  }

}
