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
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionListenerManager implements GenericFutureListener<ChannelFuture> {
  private static final Logger logger = LoggerFactory.getLogger(ConnectionListenerManager.class);
  private List<ConnectionRequest> requests;
  private PyChannel pyChannel;
  private volatile boolean connecting;

  public ConnectionListenerManager(PyChannel pyChannel) {
    this.requests = new LinkedList<>();
    this.pyChannel = pyChannel;
    this.connecting = false;
  }

  public List<ConnectionRequest> getRequests() {
    return requests;
  }

  public void add(ConnectionRequest request) {
    requests.add(request);
  }

  public void notifyAllListeners() {
    Channel channel = pyChannel.get();
    logger.warn("notifyAllListeners channel={}", channel);

    for (ConnectionRequest listener : requests) {
      try {
        logger.warn("notifyAllListeners listener={}", listener);
        listener.complete(channel);
      } catch (Exception e) {
        logger.warn("fail to notifyAllListeners connection listener, channel={}", pyChannel);
      }
    }

    requests.clear();
  }

  @Override
  public void operationComplete(ChannelFuture future) throws Exception {
    Channel channel = future.channel();
    if (future.isSuccess()) {
      logger.warn("connection successfully, channel={}", channel);
      pyChannel.set(channel);
    } else {
      logger.info("fail to connection to {}, cause:", pyChannel.getEndPoint(), future.cause());
      pyChannel.set(null);
    }
  }

  public boolean isConnecting() {
    return connecting;
  }

  public void setConnecting(boolean connecting) {
    this.connecting = connecting;
  }

  @Override
  public String toString() {
    return "ConnectionListenerManager{" + "requests=" + requests.size() + ", pyChannel=" + pyChannel
        + ", connecting=" + connecting + '}';
  }
}
