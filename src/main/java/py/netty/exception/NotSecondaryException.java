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

public class NotSecondaryException extends AbstractStatusException {
  private static final long serialVersionUID = -3628483172089682197L;

  public NotSecondaryException(long volumeId, int segIndex, int status) {
    super(ExceptionType.NOTSECONDARY, volumeId, segIndex, status);
  }

  public static NotSecondaryException fromBuffer(ByteBuf buffer) {
    NotSecondaryException exception = new NotSecondaryException(buffer.readLong(), buffer.readInt(),
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

  @Override
  public String toString() {
    return "NotSecondaryException [super=" + super.toString() + "]";
  }
}
