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

package py.client.thrift;

import org.apache.commons.lang.Validate;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.rpc.share.PyDynamicBuffer;

public class PyAsyncClientTransport extends TTransport {
  private static final Logger logger = LoggerFactory.getLogger(PyAsyncClientTransport.class);
  private final NiftyClientChannel channel;
  private PyDynamicBuffer outputBuffer;

  public PyAsyncClientTransport(NiftyClientChannel channel) {
    this.channel = channel;
    outputBuffer = new PyDynamicBuffer();

    outputBuffer.setWriteIndex(FramedClientConnector.LENGTH_FIELD_LENGTH);
  }

  @Override
  public boolean isOpen() {
    return channel.getNettyChannel().isOpen();
  }

  @Override
  public void open() {
    if (!isOpen()) {
      throw new IllegalStateException("PyAsyncClientTransport requires an already-opened channel");
    }
  }

  @Override
  public void close() {
    channel.close();
  }

  @Override
  public void write(byte[] buf, int off, int len) {
    outputBuffer.write(buf, off, len);
  }

  public void writeAndFlush(int seqId, NiftyClientChannel.Listener listener, long timeout)
      throws TTransportException {
    try {
      ChannelBuffer bufferToSend = outputBuffer.getBuffer();
      Validate.isTrue(bufferToSend.readableBytes() >= FramedClientConnector.LENGTH_FIELD_LENGTH);
      int bodySize = bufferToSend.readableBytes() - FramedClientConnector.LENGTH_FIELD_LENGTH;
      ChannelBuffer temp = bufferToSend.duplicate();
      temp.clear();
      temp.writeInt(bodySize);
      channel.sendAsynchronousRequest(seqId, bufferToSend, false, listener, timeout);
    } catch (TException e) {
      logger.error("write and flush failure", e);
      throw new TTransportException(e);
    } catch (Exception e) {
      logger.error("caught an exception", e);
      throw new TTransportException(e);
    }
  }

  @Override
  public int read(byte[] buf, int off, int len) throws TTransportException {
    throw new UnsupportedOperationException();
  }
}
