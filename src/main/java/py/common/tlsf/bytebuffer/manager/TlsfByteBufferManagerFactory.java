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

package py.common.tlsf.bytebuffer.manager;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory uses the singleton pattern to create TLSFByteBufferManager.
 */
public class TlsfByteBufferManagerFactory {
  private static final Object lock = new Object();
  private static final Logger logger = LoggerFactory.getLogger(TlsfByteBufferManagerFactory.class);

  private static TlsfByteBufferManager tlsfByteBufferManager = null;

  public static void init(int sizeAlignment, int size, boolean addressAligned) {
    if (size < 0) {
      throw new RuntimeException("size is too large, and it is overflowed");
    }

    synchronized (lock) {
      if (tlsfByteBufferManager != null) {
        logger.warn("TLSFByteBufferManager has been initialized");
        return;
      }
    }

    tlsfByteBufferManager = new TlsfByteBufferManager(sizeAlignment, size, addressAligned);
  }

  public static TlsfByteBufferManager instance() {
    return tlsfByteBufferManager;
  }

  public static TlsfByteBufferManager build(int size, int sizeAlignment) {
    Validate.isTrue(size > 0 && sizeAlignment > 0);
    TlsfByteBufferManager tlsfByteBufferManager = new TlsfByteBufferManager(sizeAlignment, size,
        true);
    return tlsfByteBufferManager;
  }
}