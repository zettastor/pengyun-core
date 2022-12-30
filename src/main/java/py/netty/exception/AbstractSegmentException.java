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
