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
