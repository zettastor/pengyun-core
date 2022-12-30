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
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import java.net.SocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.netty.client.MessageTimeManager;
import py.netty.core.nio.SingleThreadEventLoop;
import py.netty.exception.DisconnectionException;
import py.netty.message.Message;
import py.netty.message.SendMessage;

public class RequestTimeoutHandlerGeneric extends ChannelOutboundHandlerAdapter {
  private static final Logger logger = LoggerFactory.getLogger(RequestTimeoutHandlerGeneric.class);
  private final MessageTimeManager manager;
  private ChannelHandlerContext ctx;
  private int nwrite = 0;
  private SingleThreadEventLoop singleThreadEventLoop;
  private TimeToFlushTask flushTask;

  public RequestTimeoutHandlerGeneric(MessageTimeManager manager) {
    this.manager = manager;
    flushTask = new TimeToFlushTask();
  }

  @Override
  public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
      SocketAddress localAddress,
      ChannelPromise promise) throws Exception {
    if (this.ctx == null) {
      this.ctx = ctx;
      singleThreadEventLoop = getEventLoopFromCtx(ctx);
      logger.warn("connected");
    } else {
      logger.warn("connected again?");
    }
    ctx.connect(remoteAddress, localAddress, promise);
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
      throws Exception {
    SendMessage message = (SendMessage) msg;
    if (addTimerManager(ctx, message)) {
      if (message.getBuffer() != null) {
        nwrite++;
        if (nwrite == 1) {
          singleThreadEventLoop.executeAfterEventLoopIteration(flushTask);
        }
        ctx.write(message.getBuffer(), promise);
      }
    } else {
      logger.warn("can't add timer for message {}", message);
    }
  }

  @Override
  public void flush(ChannelHandlerContext ctx) throws Exception {
    nwrite = 0;
    ctx.flush();
  }

  public boolean addTimerManager(ChannelHandlerContext ctx, Message msg) {
    boolean isBroken = manager.isBroken();
    if (isBroken) {
      try {
        msg.getCallback().fail(new DisconnectionException(
            "channel=" + ctx.channel() + " already broken, message=" + msg));
      } catch (Exception e) {
        logger.warn("caught an exception", e);
      }
      return false;
    }

    manager.addTimer(msg.getRequestId(), msg.getCallback());
    return true;
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.warn("caught an exception, size={}", manager.getPendingMessageCount(), cause);
    ctx.fireExceptionCaught(cause);
  }

  private SingleThreadEventLoop getEventLoopFromCtx(ChannelHandlerContext ctx) {
    EventLoop eventLoop = ctx.channel().eventLoop();
    if (!(eventLoop instanceof SingleThreadEventLoop)) {
      logger.error("event loop is not SingleThreadEventLoop");
      return null;
    } else {
      return (SingleThreadEventLoop) eventLoop;
    }
  }

  class TimeToFlushTask implements Runnable {
    @Override
    public void run() {
      if (nwrite > 0) {
        try {
          flush(ctx);
        } catch (Exception e) {
          logger.warn("something wrong with calling flush");
        }
      }
    }
  }
}
