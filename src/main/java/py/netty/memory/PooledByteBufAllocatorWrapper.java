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
