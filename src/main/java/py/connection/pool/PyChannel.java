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
