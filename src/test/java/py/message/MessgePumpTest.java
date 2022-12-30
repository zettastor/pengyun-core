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

package py.message;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.message.exceptions.MessageHandlerMissingException;
import py.message.exceptions.MessageMapMissingException;
import py.message.impl.Message;
import py.test.TestBase;

public class MessgePumpTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(MessgePumpTest.class);

  public void init() throws Exception {
    super.init();
  }

  @Test
  public void testPostMessage() throws InterruptedException {
    Sender sender = new Sender();

    Receiver receiver = new Receiver();
    receiver.regist();

    int i = 0;
    while (++i < 10) {
      DataOfMessageA messageData = new DataOfMessageA();
      MessageA<DataOfMessageA> messageA = new MessageA<DataOfMessageA>();
      messageA.setData(messageData);

      sender.postMessage(messageA, receiver);

      logger.debug("**************counter is {}", i);
    }
    Assert.assertEquals(10, i);
    logger.debug("end of send message test");

    Thread.sleep(2000);
    receiver.unregist();
  }

  @Test
  public void testSendMessage()
      throws MessageMapMissingException, MessageHandlerMissingException, Exception {
    Receiver receiver = new Receiver();
    receiver.regist();

    DataOfMessageA messageData = new DataOfMessageA();
    MessageA<DataOfMessageA> messageA = new MessageA<DataOfMessageA>();
    messageA.setData(messageData);

    logger.debug("Message name: {}", messageA.getClass());
    Sender sender = new Sender();
    sender.sendMessage(messageA, receiver);

    int i = 0;
    logger.debug("**************counter is {}", i);

    logger.debug("end of send message test");

    Thread.sleep(2000);
    receiver.unregist();
  }

  @Test
  public void testSendBroadcastMessage() throws InterruptedException {
    Sender sender = new Sender();

    Receiver receiver = new Receiver();
    receiver.regist();

    int i = 0;

    sender.postMessage(new MessageB<String>());

    logger.debug("**************counter is {}", i);

    logger.debug("end of send message test");

    Thread.sleep(2000);
    receiver.unregist();
  }

  @After
  public void afterTest() throws InterruptedException {
    Thread.sleep(1000);
  }

  private class MessageA<DataT> extends Message<DataT> {
    public MessageA() {
      this.name = "MessageA";
    }

    @Override
    public String toString() {
      return "MessageA [uuid=" + uuid + ", name=" + name + "]";
    }
  }

  private class MessageB<DataT> extends Message<DataT> {
    public MessageB() {
      this.name = "MessageB";
    }

    @Override
    public String toString() {
      return "MessageB [uuid=" + uuid + ", name=" + name + "]";
    }

  }

  private class MessageHandlerA implements IfaceMessageHandler {
    @Override
    public void execute(InterfaceMessage<?> message) {
      logger.debug("MessageHandlerA process message: {}. Message data is {}", message,
          message.getData());
    }
  }

  private class MessageHandlerB implements IfaceMessageHandler {
    @Override
    public void execute(InterfaceMessage<?> message) {
      logger.debug("MessageHandlerB process message: {}", message);
    }
  }

  private class Sender extends py.message.impl.Sender {
  }

  private class Receiver extends py.message.impl.Receiver {
    @Override
    public void buildMessageMap() {
      this.messageMap.put(MessageA.class, new MessageHandlerA());
      this.messageMap.put(MessageB.class, new MessageHandlerB());
      logger.debug("Messge map is {}", messageMap);
    }

    @Override
    public String toString() {
      return "Receiver [dispatcher=" + dispatcher + ", messageMap=" + messageMap + "]";
    }
  }

  private class DataOfMessageA {
    private long test = 1L;
    private String strTest = "data of message-a";

    @Override
    public String toString() {
      return "DataOfMessageA [lTest=" + test + ", strTest=" + strTest + "]";
    }
  }
}
