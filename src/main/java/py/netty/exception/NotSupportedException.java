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

public class NotSupportedException extends AbstractNettyException {
  private static final long serialVersionUID = 5740216755195282009L;
  private int methodType;
  private boolean server;

  public NotSupportedException(int methodType) {
    super(ExceptionType.NOTSUPPORTED);
    this.methodType = methodType;
  }

  public static NotSupportedException fromBuffer(ByteBuf buffer) {
    NotSupportedException exception = new NotSupportedException(buffer.readInt());
    exception.setServer(buffer.readByte() != 0);
    return exception;
  }

  @Override
  public void toBuffer(ByteBuf buffer) {
    buffer.writeInt(getExceptionType().getValue());
    buffer.writeInt(getMethodType());
    buffer.writeByte((byte) (server ? 1 : 0));
  }

  public int getMethodType() {
    return methodType;
  }

  public void setMethodType(int methodType) {
    this.methodType = methodType;
  }

  public boolean isServer() {
    return server;
  }

  public void setServer(boolean server) {
    this.server = server;
  }

  @Override
  public int getSize() {
    return Integer.BYTES + Integer.BYTES + Byte.BYTES;
  }

  @Override
  public String toString() {
    return "NotSupportedException [methodType=" + methodType + ", server=" + server + ", super="
        + super.toString()
        + "]";
  }

}
