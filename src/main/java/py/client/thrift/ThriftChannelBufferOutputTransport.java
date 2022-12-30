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

package py.client.thrift;

import javax.annotation.concurrent.NotThreadSafe;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

@NotThreadSafe
public class ThriftChannelBufferOutputTransport extends TTransport {
  private static final int DEFAULT_MINIMUM_SIZE = 4096;

  private static final int UNDER_USE_THRESHOLD = 100;
  private final int minimumSize;
  private ChannelBuffer outputBuffer;
  private int bufferUnderUsedCounter;
  private ChannelBuffer header;

  public ThriftChannelBufferOutputTransport() {
    this(DEFAULT_MINIMUM_SIZE);
  }

  public ThriftChannelBufferOutputTransport(int minimumSize) {
    this.minimumSize = Math.min(DEFAULT_MINIMUM_SIZE, minimumSize);
    outputBuffer = ChannelBuffers.dynamicBuffer(this.minimumSize);
    header = outputBuffer.slice(0, 4);
    header.clear();
  }

  @Override
  public boolean isOpen() {
    return true;
  }

  @Override
  public void open() throws TTransportException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int read(byte[] buf, int off, int len) throws TTransportException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void write(byte[] buf, int off, int len) throws TTransportException {
    outputBuffer.writeBytes(buf, off, len);
  }

  public void resetOutputBuffer() {
    int shrunkenSize = shrinkBufferSize();

    if (outputBuffer.writerIndex() < shrunkenSize) {
      ++bufferUnderUsedCounter;
    } else {
      bufferUnderUsedCounter = 0;
    }

    if (shouldShrinkBuffer()) {
      outputBuffer = ChannelBuffers.dynamicBuffer(shrunkenSize);
      bufferUnderUsedCounter = 0;
      header = outputBuffer.slice(0, 4);
      header.clear();
    } else {
      outputBuffer.clear();
      header.clear();
    }

  }

  public ChannelBuffer getOutputBuffer() {
    return outputBuffer;
  }

  public ChannelBuffer getHeader() {
    return header;
  }

  @SuppressWarnings("PMD.UselessParentheses")
  private boolean shouldShrinkBuffer() {
    return bufferUnderUsedCounter > UNDER_USE_THRESHOLD
        && shrinkBufferSize() >= minimumSize;
  }

  private int shrinkBufferSize() {
    return outputBuffer.capacity() >> 1;
  }
}
