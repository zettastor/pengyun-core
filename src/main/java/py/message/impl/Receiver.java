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

package py.message.impl;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.message.IfaceMessageHandler;
import py.message.InterfaceReceiver;
import py.message.exceptions.MessageMapMissingException;

public abstract class Receiver implements InterfaceReceiver {
  private static final Logger logger = LoggerFactory.getLogger(Receiver.class);
  protected MessageDispatcher dispatcher = MessageDispatcher.getInstance();
  protected Map<Class<?>, IfaceMessageHandler> messageMap = null;

  public Receiver() {
    dispatcher.start();
    messageMap = new HashMap<Class<?>, IfaceMessageHandler>();
    try {
      buildMessageMap();

      this.regist();
    } catch (Exception e) {
      logger.error("Caught an exception when built message map", e);
    }
  }

  @Override
  public Map<Class<?>, IfaceMessageHandler> getMessageMap() throws MessageMapMissingException {
    if (messageMap == null) {
      throw new MessageMapMissingException();
    }
    return messageMap;
  }

  @Override
  public synchronized void regist() {
    dispatcher.addLisener(this);
  }

  @Override
  public synchronized void unregist() {
    dispatcher.removeLisener(this);
  }

  @Override
  public String toString() {
    return "Receiver [dispatcher=" + dispatcher + ", messageMap=" + messageMap + "]";
  }

}
