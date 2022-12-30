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

package py.netty.core;

import com.google.protobuf.AbstractMessageLite;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Method;
import py.netty.exception.AbstractNettyException;
import py.netty.exception.InvalidProtocolException;
import py.netty.exception.NotSupportedException;
import py.netty.message.Header;
import py.netty.message.Message;

public interface Protocol {
  public ByteBuf encodeRequest(Header header, AbstractMessageLite metadata)
      throws InvalidProtocolException;

  public ByteBuf encodeResponse(Header header, AbstractMessageLite metadata)
      throws InvalidProtocolException;

  public Object decodeRequest(Message msg) throws InvalidProtocolException;

  public Object decodeResponse(Message msg) throws InvalidProtocolException;

  public AbstractNettyException decodeException(ByteBuf buffer) throws InvalidProtocolException;

  public ByteBuf encodeException(long requestId, AbstractNettyException e);

  public Method getMethod(int methodType) throws NotSupportedException;
}
