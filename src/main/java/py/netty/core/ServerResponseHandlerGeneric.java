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
