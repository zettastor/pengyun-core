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
