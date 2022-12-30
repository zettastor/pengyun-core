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

package py.connection.pool;

import io.netty.channel.Channel;
import py.common.struct.EndPoint;

class PyChannel {
  private final EndPoint endPoint;
  private volatile Channel channel;
  private long lastIoTime;

  private boolean closing;

  public PyChannel(EndPoint endPoint) {
    this.lastIoTime = System.currentTimeMillis();
    this.channel = null;
    this.endPoint = endPoint;
  }

  public Channel get() {
    return channel;
  }

  public Channel set(Channel newChannel) {
    Channel oldChannel = channel;
    channel = newChannel;
    return oldChannel;
  }

  public long getLastIoTime() {
    return lastIoTime;
  }

  public void setLastIoTime(long lastIoTime) {
    this.lastIoTime = lastIoTime;
  }

  public EndPoint getEndPoint() {
    return endPoint;
  }

  public boolean isClosing() {
    return closing;
  }

  public void setClosing(boolean closing) {
    this.closing = closing;
  }

  @Override
  public String toString() {
    return "PYChannel{" + "channel=" + channel + ", lastIoTime=" + lastIoTime + ", endPoint="
        + endPoint
        + ", closing=" + closing + '}';
  }
}
