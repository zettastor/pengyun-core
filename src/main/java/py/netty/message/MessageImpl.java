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

package py.netty.message;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.netty.core.MethodCallback;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class MessageImpl<T extends Object> implements Message<T> {
  private static final Logger logger = LoggerFactory.getLogger(MessageImpl.class);

  private final Header header;
  private ByteBuf buffer;

  public MessageImpl(Header header, ByteBuf buffer) {
    this.header = header;
    this.buffer = buffer;
  }

  public Header getHeader() {
    return header;
  }

  @Override
  public long getRequestId() {
    return header.getRequestId();
  }

  @Override
  public ByteBuf getBuffer() {
    return buffer;
  }

  @Override
  public boolean release() {
    return release(1);
  }

  @Override
  public boolean release(int decrement) {
    boolean release = true;
    if (buffer != null) {
      
      release = buffer.release(decrement);
      if (release) {
        buffer = null;
      }
    }
    return release;
  }

  public MethodCallback<T> getCallback() {
    throw new NotImplementedException();
  }

  @Override
  public void releaseReference() {
    this.buffer = null;
  }

  @Override
  public String toString() {
    return "MessageImpl [header=" + header + "]";
  }

  @Override
  public int refCnt() {
    return buffer.refCnt();
  }

  @Override
  public ReferenceCounted retain() {
    buffer.retain();
    return this;
  }

  @Override
  public ReferenceCounted retain(int increment) {
    buffer.retain(increment);
    return this;
  }

  @Override
  public ReferenceCounted touch() {
    if (buffer != null) {
      buffer.touch();
    }
    return this;
  }

  @Override
  public ReferenceCounted touch(Object hint) {
    if (buffer != null) {
      buffer.touch(hint);
    }
    return this;
  }
}
