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
import py.netty.core.nio.SingleThreadEventLoop;

public class ServerResponseHandlerGeneric extends ChannelOutboundHandlerAdapter {
  private static final Logger logger = LoggerFactory
      .getLogger(ServerResponseHandlerGeneric.class);
  private ChannelHandlerContext ctx;
  private int nwrite = 0;

  @Override
  public void bind(ChannelHandlerContext ctx, SocketAddress localAddress,
      ChannelPromise promise) throws Exception {
    ctx.bind(localAddress, promise);
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg,
      ChannelPromise promise) throws Exception {
    if (this.ctx == null) {
      this.ctx = ctx;
      logger.debug("done with initialization");
    }

    ctx.write(msg, promise);
    nwrite++;
    if (nwrite == 1) {
      logger.info("get the first write");

    } else {
      logger.info("get the {} write", nwrite);
    }
  }

  @Override
  public void flush(ChannelHandlerContext ctx) throws Exception {
    logger.info("reset the number of writes to 0");
    nwrite = 0;
    ctx.flush();
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
      logger.info("timeToFlushTask get called. nWrite is {} ", nwrite);
      if (nwrite > 0) {
        logger.info("the number of writes before the flush is {}, now flushing data", nwrite);

        try {
          flush(ctx);
        } catch (Exception e) {
          logger.warn("something wrong with calling flush");
        }
      } else {
        logger.info("nWrite is zero");
      }
    }
  }
}
