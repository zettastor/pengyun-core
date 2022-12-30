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

package py.netty.core;

import io.netty.buffer.ByteBufAllocator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import py.netty.message.MethodTypeInterface;

public abstract class ProtocolFactory {
  private static final AtomicLong requestId = new AtomicLong(0);
  protected ByteBufAllocator allocator;
  protected Function<Integer, MethodTypeInterface> getMethodTypeInterface;

  public static long getRequestId() {
    return requestId.getAndIncrement();
  }

  public abstract Protocol getProtocol();

  public void setByteBufAllocator(ByteBufAllocator allocator) {
    this.allocator = allocator;
  }

  public Function<Integer, MethodTypeInterface> getGetMethodTypeInterface() {
    return getMethodTypeInterface;
  }
}
