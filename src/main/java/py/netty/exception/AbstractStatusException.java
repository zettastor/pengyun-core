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
import org.apache.commons.lang3.Validate;

public abstract class AbstractStatusException extends AbstractNettyException {
  private static final long serialVersionUID = -2121925564385427775L;
  private final long volumeId;
  private final int segIndex;
  private final int status;
  private byte[] membership;

  AbstractStatusException(ExceptionType exceptionType, long volumeId, int segIndex, int status) {
    super(exceptionType);
    this.status = status;
    this.volumeId = volumeId;
    this.segIndex = segIndex;
    this.membership = null;
  }

  public static AbstractStatusException fromBuffer(ByteBuf buffer) {
    NotPrimaryException exception = new NotPrimaryException(buffer.readLong(), buffer.readInt(),
        buffer.readInt());
    int membershipLength = buffer.readInt();
    if (buffer.readableBytes() != membershipLength) {
      Validate.isTrue(false,
          "exception: " + exception + "readableBytes: " + buffer + ", expected length: "
              + membershipLength);
    }

    byte[] membership = new byte[buffer.readableBytes()];
    buffer.readBytes(membership);
    exception.setMembership(membership);
    return exception;
  }

  public int getStatus() {
    return status;
  }

  public long getVolumeId() {
    return volumeId;
  }

  public int getSegIndex() {
    return segIndex;
  }

  @Override
  public void toBuffer(ByteBuf buffer) {
    buffer.writeInt(getExceptionType().getValue());
    buffer.writeLong(volumeId);
    buffer.writeInt(segIndex);
    buffer.writeInt(status);
    if (membership == null) {
      buffer.writeInt(0);
    } else {
      buffer.writeInt(membership.length);
      buffer.writeBytes(membership);
    }
  }

  public byte[] getMembership() {
    return membership;
  }

  public void setMembership(byte[] membership) {
    this.membership = membership;
  }

  @Override
  public int getSize() {
    return Integer.BYTES * 4 + Long.BYTES + (membership == null ? 0 : membership.length);
  }

  @Override
  public String toString() {
    return "AbstractStatusException [super=" + super.toString() + ", volumeId=" + volumeId
        + ", segIndex=" + segIndex
        + ", status=" + status + ", membership length=" + (membership == null ? 0
        : membership.length) + "]";
  }

}
