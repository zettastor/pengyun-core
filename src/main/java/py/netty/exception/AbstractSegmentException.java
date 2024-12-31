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

package py.netty.exception;

import io.netty.buffer.ByteBuf;

public class AbstractSegmentException extends AbstractNettyException {
  private final int segIndex;
  private final long volumeId;
  private final long instanceId;

  public AbstractSegmentException(ExceptionType type, int segIndex, long volumeId,
      long instanceId) {
    super(type);
    this.instanceId = instanceId;
    this.volumeId = volumeId;
    this.segIndex = segIndex;
  }

  public long getVolumeId() {
    return volumeId;
  }

  public long getInstanceId() {
    return instanceId;
  }

  public int getSegIndex() {
    return segIndex;
  }

  @Override
  public void toBuffer(ByteBuf buffer) {
    buffer.writeInt(getExceptionType().getValue());
    buffer.writeInt(segIndex);
    buffer.writeLong(volumeId);
    buffer.writeLong(instanceId);
  }

  @Override
  public int getSize() {
    return Integer.BYTES + Integer.BYTES + Long.BYTES + Long.BYTES;
  }

  @Override
  public String toString() {
    return "AbstractSegmentException [super=" + super.toString() + ", instanceId=" + instanceId
        + ", volumeId="
        + volumeId + ", segIndex=" + segIndex + "]";
  }
}
