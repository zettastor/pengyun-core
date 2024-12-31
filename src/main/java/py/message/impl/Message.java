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
