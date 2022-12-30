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

public class ServerResponseHandler extends ChannelOutboundHandlerAdapter {
  private static final Logger logger = LoggerFactory.getLogger(ServerResponseHandler.class);
  private ChannelHandlerContext ctx;
  private PyTimeToFlush ttf;

  @Override
  public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
      throws Exception {
    ctx.bind(localAddress, promise);
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
      throws Exception {
    if (this.ctx == null) {
      this.ctx = ctx;
      SingleThreadEventLoop singleThreadEventLoop = getEventLoopFromCtx(ctx);
      if (ttf == null) {
        ttf = new PyTimeToFlushImpl(singleThreadEventLoop);
      }

      singleThreadEventLoop.setTimeToFlushCallback(ttf);
      logger.debug("done with initialization");
    }

    SingleThreadEventLoop singleThreadEventLoop = getEventLoopFromCtx(ctx);
    singleThreadEventLoop.addChannelToFlush(ctx);
    ttf.incRequestCount();
    ctx.write(msg, promise);
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
}
