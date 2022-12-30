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

package py.zookeeper;

import java.util.ArrayList;
import java.util.List;

public class ZkClientFactory {
  private int sessionTimeout;
  private String serverAddress;

  public ZkClientFactory(String serverAddress, int sessionTimeout) {
    this.sessionTimeout = sessionTimeout;
    this.serverAddress = serverAddress;
  }

  public ZkClient generate(List<ZkListener> listeners) throws ZkException {
    return new ZkClientImpl(serverAddress, sessionTimeout, listeners);
  }

  public ZkClient generate(ZkListener listener) throws ZkException {
    List<ZkListener> listeners = new ArrayList<ZkListener>();
    listeners.add(listener);
    return new ZkClientImpl(serverAddress, sessionTimeout, listeners);
  }

  public int getSessionTimeout() {
    return sessionTimeout;
  }

  public void setSessionTimeout(int sessionTimeout) {
    this.sessionTimeout = sessionTimeout;
  }

  public String getServerAddress() {
    return serverAddress;
  }

  public void setServerAddress(String serverAddress) {
    this.serverAddress = serverAddress;
  }

}
