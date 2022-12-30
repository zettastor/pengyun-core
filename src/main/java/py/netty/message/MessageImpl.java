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
