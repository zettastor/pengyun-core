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

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

/**
 * There are three types of bytebuffer: <br/> 1. direct byte buffer who directly operate on memory
 * <br/> 2. byte buffer who has backup array <br/> 3. other <br/> For type 1 and type 2, we can copy
 * data in bulk. For type 3, we just copy data byte by byte. When bulk copy data between type1 and
 * {@link Unsafe}, we need to know address in the buffer where the buffer array start from. The
 * appendix stores the address.
 *
 */
public class ByteBufferAppendix {
  private static final Logger logger = LoggerFactory.getLogger(ByteBufferAppendix.class);

  /**
   * The memory base address in direct byte buffer.
   */
  private long baseAddress;

  private ByteBufferAppendix(ByteBuffer buffer) throws Exception {
    Validate.isTrue(buffer.isDirect());

    try {
      Method method = buffer.getClass().getDeclaredMethod("address");
      method.setAccessible(true);
      baseAddress = (long) method.invoke(buffer);
    } catch (Exception e) {
      logger.debug("Caught an exception", e);
      throw new RuntimeException("Unable to get array base address in direct buffer");
    }
  }

  public static ByteBufferAppendix getAppendix(ByteBuffer buffer) {
    ByteBufferAppendix appendix = null;

    if (buffer.isDirect()) {
      try {
        appendix = new ByteBufferAppendix(buffer);
      } catch (Exception e) {
        logger.error("caught exception", e);
      }
    }

    return appendix;
  }

  public long getBaseAddress() {
    return baseAddress;
  }

  public void setBaseAddress(long baseAddress) {
    this.baseAddress = baseAddress;
  }
}
