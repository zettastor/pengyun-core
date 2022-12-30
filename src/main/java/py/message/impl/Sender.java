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

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.message.IfaceMessageHandler;
import py.message.InterfaceMessage;
import py.message.InterfaceReceiver;
import py.message.InterfaceSender;
import py.message.exceptions.MessageHandlerMissingException;
import py.message.exceptions.MessageMapMissingException;

public class Sender implements InterfaceSender {
  private static final Logger logger = LoggerFactory.getLogger(Sender.class);
  protected MessageDispatcher dispatcher = MessageDispatcher.getInstance();

  public Sender() {
    dispatcher.start();
  }

  @Override
  public void sendMessage(InterfaceMessage<?> message, InterfaceReceiver receiver)
      throws MessageMapMissingException, MessageHandlerMissingException, Exception {
    Map<Class<?>, IfaceMessageHandler> destObjMessageMap = receiver.getMessageMap();
    if (destObjMessageMap == null) {
      throw new MessageMapMissingException();
    }

    IfaceMessageHandler handler = destObjMessageMap.get(message.getClass());
    if (handler == null) {
      throw new MessageHandlerMissingException();
    } else {
      try {
        handler.execute(message);
      } catch (Exception e) {
        logger.error("Caught an exception when deal with message: {}", message, e);
        throw e;
      }
    }
  }

  @Override
  public void postMessage(InterfaceMessage<?> message, InterfaceReceiver receiver) {
    logger.debug("Message name: {}", message.getClass());
    this.dispatcher.add(message, receiver);
  }

  @Override
  public void postMessage(InterfaceMessage<?> message) {
    logger.debug("Going to broadcast message: {}", message);
    this.dispatcher.add(message);
  }

  @Override
  public String toString() {
    return "Sender [dispatcher=" + dispatcher + "]";
  }

}
