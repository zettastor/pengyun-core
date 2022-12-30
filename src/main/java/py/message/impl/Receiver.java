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
