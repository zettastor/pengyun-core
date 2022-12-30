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

public class SegmentNotFoundException extends AbstractSegmentException {
  private static final long serialVersionUID = -4302460900961353619L;

  public SegmentNotFoundException(int segIndex, long volumeId, long instanceId) {
    super(ExceptionType.SEGMENTNOTFOUND, segIndex, volumeId, instanceId);
  }

  public static SegmentNotFoundException fromBuffer(ByteBuf buffer) {
    return new SegmentNotFoundException(buffer.readInt(), buffer.readLong(), buffer.readLong());
  }

  @Override
  public String toString() {
    return "SegmentNotFoundException [super=" + super.toString() + "]";
  }
}
