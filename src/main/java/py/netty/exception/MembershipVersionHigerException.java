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

public class MembershipVersionHigerException extends AbstractNettyException {
  private final long volumeId;
  private final int segIndex;
  private byte[] membership;

  public MembershipVersionHigerException(long volumeId, int segIndex) {
    super(ExceptionType.MEMBERSHIPHIGER);
    this.volumeId = volumeId;
    this.segIndex = segIndex;
  }

  public static MembershipVersionHigerException fromBuffer(ByteBuf buffer) {
    MembershipVersionHigerException exception = new MembershipVersionHigerException(
        buffer.readLong(),
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
    if (membership == null) {
      buffer.writeInt(0);
    } else {
      buffer.writeInt(membership.length);
      buffer.writeBytes(membership);
    }
  }

  @Override
  public int getSize() {
    return Integer.BYTES * 3 + Long.BYTES + (membership == null ? 0 : membership.length);
  }

  @Override
  public String toString() {
    return "MembershipVersionHigerException [super=" + super.toString() + ", volumeId=" + volumeId
        + ", segIndex="
        + segIndex + ", membership length=" + (membership == null ? 0 : membership.length) + "]";
  }

  public byte[] getMembership() {
    return membership;
  }

  public void setMembership(byte[] membership) {
    this.membership = membership;
  }

}
