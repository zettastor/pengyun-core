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
