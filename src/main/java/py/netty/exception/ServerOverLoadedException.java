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
