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
