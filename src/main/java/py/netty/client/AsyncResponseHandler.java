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

package py.netty.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.netty.core.MethodCallback;
import py.netty.core.Protocol;
import py.netty.core.ResponseTimeoutHandler;
import py.netty.exception.GenericNettyException;
import py.netty.message.Message;
import py.netty.message.MethodTypeInterface;

@SuppressWarnings({"rawtypes", "unchecked"})
public class AsyncResponseHandler extends ResponseTimeoutHandler {
  private static final Logger logger = LoggerFactory.getLogger(AsyncResponseHandler.class);

  private final MessageTimeManager messageTimeManager;
  private final Protocol protocol;
  private ExecutorService executor;
  private Function<Integer, MethodTypeInterface> getMethodTypeInterface;

  public AsyncResponseHandler(Protocol protocol,
      Function<Integer, MethodTypeInterface> getMethodTypeInterface,
      MessageTimeManager messageTimeManager) {
    super(messageTimeManager);
    this.protocol = protocol;
    this.getMethodTypeInterface = getMethodTypeInterface;
    this.messageTimeManager = messageTimeManager;

  }

  public void setExecutor(ExecutorService executor) {
    this.executor = executor;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
    Message msg = (Message) object;
    if (executor == null) {
      process(msg);
      return;
    }

    executor.execute(() -> process(msg));
  }

  private void process(Message msg) {
    MethodCallback callback = getCallback(msg.getRequestId());
    if (callback == null) {
      logger.warn("the request:{} is timeout", msg.getRequestId());
      msg.release();
      return;
    }

    logger.trace("@@ complete header: {} for response", msg.getHeader());

    if (msg.getHeader().hasException()) {
      Exception exception;
      ByteBuf byteBuf = msg.getBuffer();
      try {
        exception = protocol.decodeException(byteBuf);
        logger.info("caught exception for header:{}, exception:{}", msg.getHeader(), exception);
      } catch (Throwable e) {
        exception = new GenericNettyException(e);
      } finally {
        msg.release();
      }
      callback.fail(exception);
      return;
    }

    Object response;
    try {
      response = protocol.decodeResponse(msg);
    } catch (Throwable t) {
      logger.error("can not decode the response:{}", msg.getRequestId(), t);
      msg.release();
      callback.fail(new GenericNettyException(t));
      return;
    }

    MethodTypeInterface typeInterface = getMethodTypeInterface
        .apply((int) msg.getHeader().getMethodType());
    try {
      if (typeInterface != null && typeInterface.hasDefineDecodeMethod()) {
        Validate.notNull(response);
        callback.complete(response);
      } else {
        callback.complete(null);
      }
    } catch (Throwable e) {
      logger.error("can not complete callback:{}, msg:{}, type:{}", msg.getRequestId(),
          msg.getHeader(), typeInterface, e);
      msg.release();
      throw new RuntimeException("can not complete callback" + msg.getRequestId());
    } finally {
      logger.info("nothing need to do here");
    }

    if (typeInterface == null || typeInterface.needReleaseMsgDataInRpc()) {
      msg.release();
    }
  }

}
