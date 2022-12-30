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

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.message.InterfaceMessage;

public class Message<DataT> implements InterfaceMessage<DataT> {
  private static final Logger logger = LoggerFactory.getLogger(Message.class);
  protected final UUID uuid = UUID.randomUUID();
  protected String name;
  protected DataT data;

  public Message(String name, DataT data) {
    this.name = name;
    this.data = data;
  }

  public Message(DataT data) {
    this.name = this.getClass().getName();
  }

  public Message() {
    this.name = this.getClass().getName();
    logger.warn(
        "You've invoke the constructor of [{}] which is not going to initialize the message data."
            + "So you have to use setter to set the message data, or the data will be null", name);
  }

  @Override
  public UUID uuid() {
    return uuid;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public DataT getData() {
    return data;
  }

  @Override
  public void setData(DataT data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return "Message [uuid=" + uuid + ", name=" + name + ", data=" + data + "]";
  }

}
