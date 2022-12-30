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

package py.netty.memory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.internal.PlatformDependent;

public class PooledByteBufAllocatorWrapper extends PooledByteBufAllocator {
  public static final ByteBufAllocator INSTANCE = new PooledByteBufAllocatorWrapper(
      PlatformDependent.directBufferPreferred());

  public PooledByteBufAllocatorWrapper(boolean preferDirect) {
    super(preferDirect);
  }

  @Override
  protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity) {
    ByteBuf byteBuf = super.newHeapBuffer(initialCapacity, maxCapacity);
    return byteBuf;
  }

  @Override
  protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity) {
    ByteBuf byteBuf = super.newDirectBuffer(initialCapacity, maxCapacity);
    return byteBuf;
  }

  @Override
  public CompositeByteBuf compositeHeapBuffer(int maxNumComponents) {
    CompositeByteBuf byteBuf = super.compositeHeapBuffer(maxNumComponents);
    return byteBuf;
  }

  @Override
  public CompositeByteBuf compositeDirectBuffer(int maxNumComponents) {
    CompositeByteBuf byteBuf = super.compositeDirectBuffer(maxNumComponents);
    return byteBuf;
  }
}
