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

package py.common.rpc.share;

import org.apache.commons.lang.Validate;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class PyDynamicBuffer {
  private static final int DEFAULT_INIT_BUFFER_SIZE = 1024;
  private static final int MAX_THRESHOLD_SIZE_EXPONENTIAL_INCREASE = 1024 * 1024;
  private ChannelBuffer channelBuffer;
  private int maxThresholdSizeOfExponentialIncrease = MAX_THRESHOLD_SIZE_EXPONENTIAL_INCREASE;

  public PyDynamicBuffer(int initSize, int maxThresholdSizeOfExponentialIncrease) {
    Validate.isTrue(initSize <= maxThresholdSizeOfExponentialIncrease);
    channelBuffer = ChannelBuffers.buffer(((initSize >> 10) << 10));
    this.maxThresholdSizeOfExponentialIncrease = maxThresholdSizeOfExponentialIncrease;
  }

  public PyDynamicBuffer(int initSize) {
    this(initSize, MAX_THRESHOLD_SIZE_EXPONENTIAL_INCREASE);
  }

  public PyDynamicBuffer() {
    this(DEFAULT_INIT_BUFFER_SIZE, MAX_THRESHOLD_SIZE_EXPONENTIAL_INCREASE);
  }

  public void setWriteIndex(int writeIndex) {
    channelBuffer.writerIndex(writeIndex);
  }

  public void write(byte[] src, int offset, int length) {
    int canWriteBytes = channelBuffer.writableBytes();
    if (canWriteBytes < length) {
      // Keep the current write index, after the buffer is expanded,
      // the write index should be recoveried.
      int writeIndex = channelBuffer.writerIndex();

      // For expanding the buffer, the write index should be moved to the capacity.
      channelBuffer.writerIndex(channelBuffer.capacity());

      // calculate the expend size of channelBuffer buffer
      int originSize = writeIndex;
      int increaseSize = channelBuffer.capacity();
      while ((increaseSize - originSize) < length) {
        if (increaseSize >= maxThresholdSizeOfExponentialIncrease) {
          increaseSize += maxThresholdSizeOfExponentialIncrease;
        } else {
          increaseSize <<= 1;
        }
      }

      ChannelBuffer expandBuffer = ChannelBuffers.buffer(increaseSize - originSize);
      expandBuffer.writerIndex(expandBuffer.capacity());
      channelBuffer = ChannelBuffers.wrappedBuffer(channelBuffer, expandBuffer);
      channelBuffer.writerIndex(writeIndex);
    }
    channelBuffer.writeBytes(src, offset, length);
  }

  public ChannelBuffer getBuffer() {
    return channelBuffer;
  }

  @Override
  public String toString() {
    return "PyDynamicBuffer [channelBuffer=" + channelBuffer + "]";
  }
}
