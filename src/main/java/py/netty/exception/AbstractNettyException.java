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
