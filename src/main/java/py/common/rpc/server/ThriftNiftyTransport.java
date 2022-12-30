/*
 * Copyright (C) 2012-2013 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package py.common.rpc.server;

import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.rpc.codec.DefaultThriftFrameEncoder;
import py.common.rpc.share.PyDynamicBuffer;

/**
 * Wraps incoming channel buffer into TTransport and provides a output buffer.
 */
public class ThriftNiftyTransport extends TTransport {
  private final Logger logger = LoggerFactory.getLogger(ThriftNiftyTransport.class);
  private final Channel channel;
  private final ChannelBuffer in;
  private final ThriftTransportType thriftTransportType;
  private final int initialReaderIndex;
  private final int initialBufferPosition;
  private final byte[] buffer;
  private PyDynamicBuffer out;
  private int bufferPosition;
  private int bufferEnd;

  public ThriftNiftyTransport(Channel channel, ChannelBuffer in,
      ThriftTransportType thriftTransportType) {
    this.channel = channel;
    this.in = in;

    this.thriftTransportType = thriftTransportType;
    this.out = new PyDynamicBuffer();
    out.setWriteIndex(DefaultThriftFrameEncoder.LENGTH_FIELD_LENGTH);

    this.initialReaderIndex = in.readerIndex();

    if (!in.hasArray()) {
      buffer = null;
      bufferPosition = 0;
      initialBufferPosition = bufferEnd = -1;
    } else {
      buffer = in.array();
      initialBufferPosition = bufferPosition = in.arrayOffset() + in.readerIndex();
      bufferEnd = bufferPosition + in.readableBytes();
      // Without this, reading from a !in.hasArray() buffer will advance the readerIndex
      // of the buffer, while reading from a in.hasArray() buffer will not advance the
      // readerIndex, and this has led to subtle bugs. This should help to identify
      // those problems by making things more consistent.
      in.readerIndex(in.readerIndex() + in.readableBytes());
    }
  }

  public ThriftNiftyTransport(Channel channel, ThriftMessage message) {
    this(channel, message.getBuffer(), message.getTransportType());
  }

  @Override
  public boolean isOpen() {
    return channel.isOpen();
  }

  @Override
  public void open() throws TTransportException {
    // no-op
  }

  @Override
  public void close() {
    // no-op
    channel.close();
  }

  @Override
  public int read(byte[] bytes, int offset, int length) throws TTransportException {
    if (getBytesRemainingInBuffer() >= 0) {
      int read = Math.min(getBytesRemainingInBuffer(), length);
      System.arraycopy(getBuffer(), getBufferPosition(), bytes, offset, read);
      consumeBuffer(read);
      return read;
    } else {
      int read = Math.min(in.readableBytes(), length);
      in.readBytes(bytes, offset, read);
      return read;
    }
  }

  @Override
  public int readAll(byte[] bytes, int offset, int length) throws TTransportException {
    if (read(bytes, offset, length) < length) {
      throw new TTransportException("Buffer doesn't have enough bytes to read");
    }
    return length;
  }

  @Override
  public void write(byte[] bytes, int offset, int length) throws TTransportException {
    out.write(bytes, offset, length);
  }

  public ChannelBuffer getOutputBuffer() {
    return out.getBuffer();
  }

  public ThriftTransportType getTransportType() {
    return thriftTransportType;
  }

  @Override
  public void flush() throws TTransportException {
    // Flush is a no-op: NiftyDispatcher will write the response to the Channel, in order to
    // guarantee ordering of responses when required.
  }

  @Override
  public void consumeBuffer(int len) {
    bufferPosition += len;
  }

  @Override
  public byte[] getBuffer() {
    return buffer;
  }

  @Override
  public int getBufferPosition() {
    return bufferPosition;
  }

  @Override
  public int getBytesRemainingInBuffer() {
    return bufferEnd - bufferPosition;
  }

  public int getReadByteCount() {
    if (getBytesRemainingInBuffer() >= 0) {
      return getBufferPosition() - initialBufferPosition;
    } else {
      return in.readerIndex() - initialReaderIndex;
    }
  }

  public int getWrittenByteCount() {
    return getOutputBuffer().writerIndex();
  }
}
