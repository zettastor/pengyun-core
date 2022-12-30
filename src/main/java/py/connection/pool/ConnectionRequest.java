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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionRequest implements ConnectionListener {
  public static final Logger logger = LoggerFactory.getLogger(ConnectionRequest.class);
  public final PyChannel pyChannel;

  public ConnectionRequest(PyChannel pyChannel) {
    this.pyChannel = pyChannel;
  }

  public PyChannel getPyChannel() {
    return pyChannel;
  }

  @Override
  public void complete(Channel channel) {
  }

  @Override
  public String toString() {
    return "ConnectionRequest{" + "pyChannel=" + pyChannel + '}';
  }
}
