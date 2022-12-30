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

package py.netty.server;

import com.google.protobuf.AbstractMessageLite;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.consumer.ConsumerService;
import py.netty.core.AbstractMethodCallback;
import py.netty.core.MethodCallback;
import py.netty.core.Protocol;
import py.netty.exception.AbstractNettyException;
import py.netty.exception.InvalidProtocolException;
import py.netty.exception.NotSupportedException;
import py.netty.exception.ServerOverLoadedException;
import py.netty.exception.ServerProcessException;
import py.netty.exception.TooBigFrameException;
import py.netty.message.Header;
import py.netty.message.Message;

public class AsyncRequestHandler extends SimpleChannelInboundHandler<Message> {
  public static final String className = "AsyncRequestHandler";
  private static final Logger logger = LoggerFactory.getLogger(AsyncRequestHandler.class);

  private final Object serviceObject;
  private final Protocol protocol;
  private AtomicInteger pendingRequestsCount;
  private int maxPendingRequestsCount;
  private ConsumerService<Runnable> requestExecutor;

  public AsyncRequestHandler(Object serviceObject, Protocol protocol) {
    super(false);
    this.serviceObject = serviceObject;
    this.protocol = protocol;
    this.pendingRequestsCount = new AtomicInteger(0);
  }

  public void setExecutor(ConsumerService<Runnable> executor) {
    this.requestExecutor = executor;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
    if (this.requestExecutor != null) {
      this.requestExecutor.submit(() -> process(ctx, msg));
    } else {
      process(ctx, msg);
    }
  }

  private void process(ChannelHandlerContext ctx, Message msg) {
    logger.debug("receive message: {}", msg);
    Header header = msg.getHeader();
    int pending = pendingRequestsCount.incrementAndGet();
    if (pending > maxPendingRequestsCount) {
      logger
          .info("current length: {}, max length: {}, header: {}", pending, maxPendingRequestsCount,
              header);
      fireResponse(msg, ctx, protocol.encodeException(header.getRequestId(),
          new ServerOverLoadedException(maxPendingRequestsCount, pending)));
      return;
    }

    Object object;
    try {
      object = protocol.decodeRequest(msg);
    } catch (InvalidProtocolException e) {
      logger.error("can not decode the body: {}", msg, e);
      fireResponse(msg, ctx, protocol.encodeException(header.getRequestId(), e));
      return;
    }

    Method method;
    try {
      method = protocol.getMethod(header.getMethodType());
    } catch (NotSupportedException e) {
      logger.error("can not support the method: {}", msg, e);
      e.setServer(true);
      fireResponse(msg, ctx, protocol.encodeException(header.getRequestId(), e));
      return;
    }

    try {
      MethodCallback<Object> callback = generateCallback(msg, ctx);
      Object[] args;
      if (object == null) {
        args = new Object[]{callback};
      } else {
        args = new Object[]{object, callback};
      }

      method.invoke(serviceObject, args);

    } catch (Throwable e) {
      logger.warn("can not invoke the method", e);
      fireResponse(msg, ctx,
          protocol.encodeException(header.getRequestId(), new ServerProcessException(e)));
    }

  }

  private MethodCallback<Object> generateCallback(Message msg, ChannelHandlerContext ctx) {
    final Header header = msg.getHeader();
    header.setDataLength(0);
    header.setMetadataLength(0);

    MethodCallback<Object> callback = new AbstractMethodCallback<Object>() {
      @Override
      public void complete(Object object) {
        Object response;
        try {
          response = protocol.encodeResponse(header, (AbstractMessageLite) object);
          logger.trace("@@ complete header: {} for request", header);
        } catch (InvalidProtocolException e) {
          logger.error("caught an exception", e);
          response = protocol.encodeException(header.getRequestId(), e);
        } catch (Throwable e) {
          logger.error("caught an exception", e);
          response = protocol.encodeException(header.getRequestId(), new ServerProcessException(e));
        }
        fireResponse(msg, ctx, response);
      }

      @Override
      public void fail(Exception e) {
        logger.info("caught an exception for msg: {}", msg.getRequestId(), e);
        Object response;
        if (e instanceof AbstractNettyException) {
          response = protocol.encodeException(header.getRequestId(), (AbstractNettyException) e);
        } else {
          response = protocol.encodeException(header.getRequestId(), new ServerProcessException(e));
        }

        fireResponse(msg, ctx, response);
      }

      @Override
      public ByteBufAllocator getAllocator() {
        return ctx.alloc();
      }
    };

    return callback;
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.warn("caught an exception", cause);
    if (cause instanceof TooBigFrameException) {
      TooBigFrameException e = (TooBigFrameException) cause;
      ctx.channel().writeAndFlush(protocol.encodeException(e.getHeader().getRequestId(), e))
          .addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
              logger.error("fail response channel:{}", future.channel().remoteAddress());
            }
          });
    }
  }

  private void fireResponse(Message request, ChannelHandlerContext ctx, Object msg) {
    request.release();
    pendingRequestsCount.decrementAndGet();
    ctx.channel().write(msg).addListener((ChannelFutureListener) future -> {
      if (!future.isSuccess()) {
        logger.error("fail response channel:{}", future.channel().remoteAddress());
      }
    });
  }

  public void setMaxPendingRequestsCount(int maxPendingRequestsCount) {
    this.maxPendingRequestsCount = maxPendingRequestsCount;
  }

  public void setPendingRequestsCount(AtomicInteger pendingRequestsCount) {
    this.pendingRequestsCount = pendingRequestsCount;
  }
}
