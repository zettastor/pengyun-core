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

package py.common.struct;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class ExchangeMessage {
  ChannelHandlerContext ctx;

  private ByteBuf buffer;
  private InetSocketAddress sockAddress;

  private int sequnceId;

  public ExchangeMessage(ChannelHandlerContext ctx, ByteBuf buffer, InetSocketAddress sockAddress) {
    this.buffer = buffer;
    this.sockAddress = sockAddress;
    this.ctx = ctx;
    this.sequnceId = buffer.getInt(ConstantField.SEQUENCEIDOFFSET);
  }

  public ChannelHandlerContext getChannel() {
    return ctx;
  }

  public ByteBuf getBuffer() {
    return buffer;
  }

  public SocketAddress getAddress() {
    return sockAddress;
  }

  public void respond(byte[] bytebuf) {
    int length = buffer.capacity() - (ConstantField.SEQUENCEIDLENGTH + ConstantField.TYPELENGTH);

    if (bytebuf.length > length) {
      respond(bytebuf, 0, bytebuf.length);
    } else {
      length = bytebuf.length;
      buffer.setInt(ConstantField.SEQUENCEIDOFFSET, sequnceId);
      buffer.setByte(ConstantField.TYPEOFFSET, ConstantField.RESPONSEEMESSAGE);
      buffer.setBytes(ConstantField.PAYLOADOFFSET, bytebuf, 0, length);

      buffer.setIndex(ConstantField.SEQUENCEIDOFFSET,
          length + (ConstantField.SEQUENCEIDLENGTH + ConstantField.TYPELENGTH));
      buffer.retain();
      ctx.writeAndFlush(new DatagramPacket(buffer, sockAddress));
    }
  }

  public void respond(byte[] bytebuf, int offset, int length) {
    ByteBuf sendbuffer = newBuffer(length + 5);

    sendbuffer.setInt(ConstantField.SEQUENCEIDOFFSET, sequnceId);
    sendbuffer.setByte(ConstantField.TYPEOFFSET, ConstantField.RESPONSEEMESSAGE);
    sendbuffer.setBytes(ConstantField.PAYLOADOFFSET, bytebuf, 0, length);

    sendbuffer.setIndex(ConstantField.SEQUENCEIDOFFSET,
        length + (ConstantField.SEQUENCEIDLENGTH + ConstantField.TYPELENGTH));
    buffer.retain();
    ctx.writeAndFlush(new DatagramPacket(sendbuffer, sockAddress));
  }

  private ByteBuf newBuffer(int length) {
    return PooledByteBufAllocator.DEFAULT.heapBuffer(length);
  }

  public int getPayloadLength() {
    return buffer.readableBytes() - ConstantField.PAYLOADOFFSET;
  }

  public ByteBuffer getPayloadByteBuffer() {
    if (buffer != null) {
      buffer.skipBytes(ConstantField.PAYLOADOFFSET);
      int len = buffer.readableBytes();
      if (len <= 0) {
        return null;
      }
      return buffer.readBytes(len).nioBuffer(0, len);
    }
    return null;
  }

  public byte[] getPayloadByteArray() {
    if (buffer != null) {
      buffer.skipBytes(ConstantField.PAYLOADOFFSET);
      int len = buffer.readableBytes();
      if (len <= 0) {
        return null;
      }
      byte[] srcdata = buffer.readBytes(len).array();
      return srcdata;
    }
    return null;
  }

  public void getPayload(byte[] dst, int dstIndex, int length) {
    System.arraycopy(buffer.array(), ConstantField.PAYLOADOFFSET, dst, dstIndex, length);

  }

  public void getPayload(ByteBuffer dst) {
    dst.put(buffer.array(), ConstantField.PAYLOADOFFSET,
        Math.min(buffer.capacity() - ConstantField.PAYLOADOFFSET, dst.remaining()));
  }

  public int getSequnceId() {
    return buffer.getInt(ConstantField.SEQUENCEIDOFFSET);
  }

  public byte getType() {
    return buffer.getByte(ConstantField.TYPEOFFSET);
  }

  public class ConstantField {
    public static final int SEQUENCEIDOFFSET = 0;
    public static final int SEQUENCEIDLENGTH = 4;
    public static final int TYPEOFFSET = 4;
    public static final int TYPELENGTH = 1;
    public static final int PAYLOADOFFSET = 5;
    public static final byte INQUIREMESSAGE = 'I';
    public static final byte RESPONSEEMESSAGE = 'R';

  }

}
