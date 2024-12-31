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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.netty.client.MessageTimeManager;
import py.netty.exception.TooBigFrameException;

public abstract class ResponseTimeoutHandler extends ChannelInboundHandlerAdapter {
  private final Logger logger = LoggerFactory.getLogger(ResponseTimeoutHandler.class);
  private final MessageTimeManager messageTimeManager;

  public ResponseTimeoutHandler(MessageTimeManager messageTimeManager) {
    this.messageTimeManager = messageTimeManager;
  }

  public MethodCallback getCallback(long requestId) {
    return messageTimeManager.removeTimer(requestId);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if (cause instanceof TooBigFrameException) {
      TooBigFrameException e = (TooBigFrameException) cause;
      MethodCallback callback = messageTimeManager.removeTimer(e.getHeader().getRequestId());
      if (callback == null) {
        logger.info("the request has timeout: {} for too large frame", e.getHeader());
        return;
      } else {
        callback.fail(e);
      }
    } else if (cause instanceof IOException) {
      logger.warn("caught an exception: {} msg: {}, just close the channel: {}, pending count={}",
          cause.getClass().getSimpleName(), cause.getMessage(), ctx.channel(),
          messageTimeManager.getPendingMessageCount());
      messageTimeManager.fireChannelClose();
    } else {
      logger.warn("caught an exception in netty", cause);
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    logger.warn("inactive the channel: {}, pending count={}", ctx.channel(),
        messageTimeManager.getPendingMessageCount());
    messageTimeManager.fireChannelClose();
    ctx.fireChannelInactive();
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent e = (IdleStateEvent) evt;
      if (e.state() == IdleState.READER_IDLE) {
        try {
          ctx.channel().read();
        } catch (Exception e1) {
          logger.warn("channel has broken: {}", ctx.channel(), e1);
        }
      }
    }
  }
}
