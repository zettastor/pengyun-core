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

import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public abstract class PyMessage extends AbstractMessageLite implements MessageCarryDataInterface {
  protected final Message metadata;
  protected final ByteBuf data;

  public PyMessage(Message metadata, ByteBuf body) {
    this.metadata = metadata;
    this.data = body;
  }

  @Override
  public ByteBuf getData() {
    return data;
  }

  @Override
  public int getDataLength() {
    return data == null ? 0 : data.readableBytes();
  }

  public int getSerializedSize() {
    if (metadata == null) {
      return 0;
    }

    return metadata.getSerializedSize();
  }

  @Override
  public void writeTo(final CodedOutputStream output) throws IOException {
    metadata.writeTo(output);
  }

  @Override
  public Message.Builder newBuilderForType() {
    return null;
  }

  @Override
  public Message.Builder toBuilder() {
    return null;
  }

  @Override
  public Message getDefaultInstanceForType() {
    return null;
  }

  @Override
  public Parser<? extends MessageLite> getParserForType() {
    return null;
  }

  @Override
  public boolean isInitialized() {
    return false;
  }

  @Override
  public String toString() {
    return "PYMessage{" + "metadata=" + metadata + '}';
  }
}
