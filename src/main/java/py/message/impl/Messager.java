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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.message.InterfaceMessage;
import py.message.InterfaceReceiver;
import py.message.InterfaceSender;
import py.message.exceptions.MessageHandlerMissingException;
import py.message.exceptions.MessageMapMissingException;

public abstract class Messager extends Receiver implements InterfaceSender {
  private static final Logger logger = LoggerFactory.getLogger(Messager.class);
  protected MessageDispatcher dispatcher = MessageDispatcher.getInstance();

  public Messager() {
    super();
  }

  @Override
  public void sendMessage(InterfaceMessage<?> message, InterfaceReceiver receiver)
      throws MessageMapMissingException, MessageHandlerMissingException, Exception {
    Sender sender = new Sender();
    sender.sendMessage(message, receiver);
  }

  @Override
  public void postMessage(InterfaceMessage<?> message, InterfaceReceiver receiver) {
    Sender sender = new Sender();
    sender.postMessage(message, receiver);
  }

  @Override
  public void postMessage(InterfaceMessage<?> message) {
    logger.debug("Going to broadcast message: {}", message);
    this.dispatcher.add(message);
  }

  @Override
  public String toString() {
    return "MessageObject [dispatcher=" + dispatcher + ", messageMap=" + messageMap + "]";
  }

}