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
