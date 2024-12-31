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
