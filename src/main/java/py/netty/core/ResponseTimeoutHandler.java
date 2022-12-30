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
