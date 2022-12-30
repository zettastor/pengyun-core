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
