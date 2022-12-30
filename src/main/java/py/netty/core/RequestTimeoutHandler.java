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
