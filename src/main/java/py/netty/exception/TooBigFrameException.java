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
import py.netty.message.Header;

public class TooBigFrameException extends AbstractNettyException {
  private static final long serialVersionUID = -6516483328493444454L;

  private final int maxFrameSize;
  private final int frameSize;
  private Header header;

  public TooBigFrameException(int maxFrameSize, int frameSize) {
    super(ExceptionType.TOOBIGFRAME);
    this.maxFrameSize = maxFrameSize;
    this.frameSize = frameSize;
  }

  public static TooBigFrameException fromBuffer(ByteBuf buffer) {
    return new TooBigFrameException(buffer.readInt(), buffer.readInt());
  }

  @Override
  public void toBuffer(ByteBuf buffer) {
    buffer.writeInt(getExceptionType().getValue());
    buffer.writeInt(maxFrameSize);
    buffer.writeInt(frameSize);
  }

  @Override
  public int getSize() {
    return Integer.BYTES * 3;
  }

  public int getMaxFrameSize() {
    return maxFrameSize;
  }

  public int getFrameSize() {
    return frameSize;
  }

  @Override
  public String toString() {
    return "TooBigFrameException [maxFrameSize=" + maxFrameSize + ", frameSize=" + frameSize + "]";
  }

  public Header getHeader() {
    return header;
  }

  public TooBigFrameException setHeader(Header header) {
    this.header = header;
    return this;
  }
}
