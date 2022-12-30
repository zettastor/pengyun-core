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