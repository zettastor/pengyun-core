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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.struct.EndPoint;

public class PyChannelPool {
  private static final Logger logger = LoggerFactory.getLogger(PyChannelPool.class);
  private final PyChannel[] pyChannels;
  private final EndPoint endpoint;
  private int robin;
  private int times;
  private ReconnectionHandler reconnectionHandler;

  public PyChannelPool(int robin, EndPoint endPoint,
      ReconnectionHandler reconnectionHandler) {
    this.pyChannels = new PyChannel[robin];
    this.robin = robin;
    this.times = 0;
    this.endpoint = endPoint;

    this.reconnectionHandler = reconnectionHandler;

    for (int i = 0; i < robin; i++) {
      pyChannels[i] = null;
    }
  }

  public EndPoint getEndpoint() {
    return endpoint;
  }

  public synchronized PyChannel get(EndPoint endPoint) {
    int index = times % robin;

    times = index + 1;
    if (pyChannels[index] != null) {
      return pyChannels[index];
    }

    logger.warn("build new connection with end point: {}", endPoint);
    pyChannels[index] = new PyChannel(endPoint);
    reconnectionHandler.reconnect(new ConnectionRequest(pyChannels[index]));
    return pyChannels[index];
  }

  public List<PyChannel> getAll() {
    List<PyChannel> channels = new ArrayList<>();
    for (int i = 0; i < pyChannels.length; i++) {
      if (pyChannels[i] != null) {
        channels.add(pyChannels[i]);
      }
    }

    return channels;
  }

  public void close() {
    for (int i = 0; i < pyChannels.length; i++) {
      if (pyChannels[i] == null) {
        continue;
      }

      Channel old = pyChannels[i].set(null);
      if (old != null) {
        old.close();
      }
    }
  }

  @Override
  public String toString() {
    return "PYChannelPool{" + "pyChannels=" + Arrays.toString(pyChannels) + ", robin=" + robin
        + ", times=" + times
        + ", endpoint=" + endpoint + ", reconnectionHandler=" + reconnectionHandler + '}';
  }
}
