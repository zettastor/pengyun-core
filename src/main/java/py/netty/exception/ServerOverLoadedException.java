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

public class ServerOverLoadedException extends AbstractNettyException {
  private static final long serialVersionUID = 7043661129141862531L;
  private int pendingRequests = 0;
  private int queueLength = 0;

  public ServerOverLoadedException(int queueLength, int pendingRequests) {
    super(ExceptionType.OVERLOADED,
        "server queue: " + queueLength + ", pending requests: " + pendingRequests);
    this.setPendingRequests(pendingRequests);
    this.setQueueLength(queueLength);
  }

  public static ServerOverLoadedException fromBuffer(ByteBuf buffer) {
    return new ServerOverLoadedException(buffer.readInt(), buffer.readInt());
  }

  public int getPendingRequests() {
    return pendingRequests;
  }

  public void setPendingRequests(int pendingRequests) {
    this.pendingRequests = pendingRequests;
  }

  public void toBuffer(ByteBuf buffer) {
    buffer.writeInt(getExceptionType().getValue());
    buffer.writeInt(queueLength);
    buffer.writeInt(pendingRequests);
  }

  public int getQueueLength() {
    return queueLength;
  }

  public void setQueueLength(int queueLength) {
    this.queueLength = queueLength;
  }

  @Override
  public int getSize() {
    return Integer.BYTES + Integer.BYTES + Integer.BYTES;
  }

}
