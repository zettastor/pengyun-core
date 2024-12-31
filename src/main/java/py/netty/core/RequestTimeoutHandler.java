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

package py.netty.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.netty.client.MessageTimeManager;
import py.netty.exception.DisconnectionException;
import py.netty.message.Message;
import py.netty.message.SendMessage;

public class RequestTimeoutHandler extends ChannelOutboundHandlerAdapter {
  private static final Logger logger = LoggerFactory.getLogger(RequestTimeoutHandler.class);
  private final MessageTimeManager manager;

  public RequestTimeoutHandler(MessageTimeManager manager) {
    this.manager = manager;
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
      throws Exception {
    SendMessage message = (SendMessage) msg;
    ByteBuf dataToSend = message.getBuffer();
    Validate.notNull(dataToSend);
    if (addTimerManager(ctx, message)) {
      try {
        ctx.write(dataToSend, promise);
        message.releaseReference();
      } catch (Exception e) {
        logger.error("caught an exception when channel write", e);
      }
    }

  }

  protected boolean addTimerManager(ChannelHandlerContext ctx, Message msg) {
    boolean isBroken = manager.isBroken();
    if (isBroken) {
      try {
        msg.getCallback().fail(new DisconnectionException(
            "channel=" + ctx.channel() + " already broken, message=" + msg));
      } catch (Exception e) {
        logger.warn("caught an exception", e);
      } finally {
        msg.release();
      }
      return false;
    }

    manager.addTimer(msg.getRequestId(), msg.getCallback());
    return true;
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.warn("caught an exception, message count:{}", manager.getPendingMessageCount(), cause);
    ctx.fireExceptionCaught(cause);
  }
}
