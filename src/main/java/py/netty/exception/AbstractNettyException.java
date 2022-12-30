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
import java.nio.charset.Charset;
import org.apache.commons.lang3.Validate;

public abstract class AbstractNettyException extends Exception {
  private static final long serialVersionUID = -8797900855979194346L;
  private static ExceptionType[] exceptionTypes = ExceptionType.values();

  static {
    for (int i = 0; i < exceptionTypes.length; i++) {
      Validate.isTrue(exceptionTypes[i].getValue() == i);
    }
  }

  private ExceptionType exceptionType;

  public AbstractNettyException(ExceptionType exceptionType, String msg) {
    super(msg);
    this.exceptionType = exceptionType;
  }

  public AbstractNettyException(ExceptionType exceptionType) {
    super();
    this.exceptionType = exceptionType;
  }

  public static AbstractNettyException parse(ByteBuf byteBuf) throws GenericNettyException {
    int exceptionType = byteBuf.readInt();
    if (exceptionType >= exceptionTypes.length) {
      return new GenericNettyException("can not parse the exception: " + exceptionType);
    }
    return exceptionTypes[exceptionType].fromBuffer(byteBuf);
  }

  public static String bufferToString(byte[] src, int off, int len) {
    return new String(src, off, len, Charset.forName("UTF-8"));
  }

  public static String bufferToString(ByteBuf buffer) {
    if (buffer.hasArray()) {
      return new String(buffer.array(), buffer.arrayOffset() + buffer.readerIndex(),
          buffer.readableBytes(),
          Charset.forName("UTF-8"));
    } else {
      byte[] data = new byte[buffer.readableBytes()];
      buffer.readBytes(data);
      return new String(data, 0, data.length, Charset.forName("UTF-8"));
    }
  }

  public static byte[] stringToBuffer(String str) {
    return str.getBytes(Charset.forName("UTF-8"));
  }

  public int getSize() {
    String msg = getMessage();
    if (msg == null || msg.isEmpty()) {
      return Integer.BYTES;
    }

    return Integer.BYTES + msg.length();
  }

  public void toBuffer(ByteBuf buffer) {
    buffer.writeInt(getExceptionType().getValue());
    String msg = getMessage();
    if (msg == null || msg.isEmpty()) {
      return;
    }

    buffer.writeBytes(AbstractNettyException.stringToBuffer(getMessage()));
  }

  public ExceptionType getExceptionType() {
    return exceptionType;
  }
}
