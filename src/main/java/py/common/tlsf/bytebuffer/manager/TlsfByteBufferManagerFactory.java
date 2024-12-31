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