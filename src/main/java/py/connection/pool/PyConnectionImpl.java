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

package py.connection.pool;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.netty.exception.DisconnectionException;
import py.netty.message.Message;

public class PyConnectionImpl implements PyConnection {
  private static final Logger logger = LoggerFactory.getLogger(PyConnectionImpl.class);

  private final PyChannel pyChannel;
  private ReconnectionHandler reconnectionHandler;

  public PyConnectionImpl(PyChannel pyChannel, ReconnectionHandler reconnectionHandler) {
    this.pyChannel = pyChannel;
    this.reconnectionHandler = reconnectionHandler;
  }

  public boolean isConnected() {
    Channel channel = pyChannel.get();
    if (channel == null) {
      return false;
    } else {
      return channel.isActive();
    }
  }

  @Override
  public void write(Message msg) {
    pyChannel.setLastIoTime(System.currentTimeMillis());
    Channel channel = pyChannel.get();

    if (channel == null) {
      logger.info("there is no channel={}, when writing, msg={}", pyChannel, msg);
      reconnectionHandler.reconnect(buildConnectionRequest(msg, pyChannel));
      return;
    }

    logger.debug("write msg to channel={}, msg={}", pyChannel, msg);

    channel.write(msg).addListener(new GenericFutureListener<ChannelFuture>() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
          logger.warn("send failure: {}, {}, {}, {} Channel is:{}, for msg:{}", future.channel(),
              future.isCancelled(), future.isDone(), future.isSuccess(), pyChannel, msg,
              future.cause());
          msg.getCallback()
              .fail(new DisconnectionException("channel=" + pyChannel + " for msg=" + msg));
        }
        logger.debug("message: {} wrote successfully", msg);
      }
    });
  }

  public ConnectionRequest buildConnectionRequest(Message msg, PyChannel pyChannel) {
    final AtomicInteger counter = new AtomicInteger(1);
    return new ConnectionRequest(pyChannel) {
      @Override
      public void complete(Channel newChannel) {
        int count = counter.decrementAndGet();
        if (count != 0) {
          logger.warn("call many times: {}", count);
          return;
        }

        if (newChannel == null) {
          logger.debug("buildConnectionRequest newChannel is null: msg={} Channel is {} ", msg,
              pyChannel);
          msg.getCallback()
              .fail(new DisconnectionException("channel=" + pyChannel + " for msg=" + msg));

          msg.release();
          return;
        }

        logger.warn("after connecting successfully, resend={}, channel={}", msg.getRequestId(),
            newChannel);
        newChannel.write(msg).addListener(new GenericFutureListener<ChannelFuture>() {
          @Override
          public void operationComplete(ChannelFuture future) throws Exception {
            if (!future.isSuccess()) {
              logger
                  .warn("send failure: {}, {}, {}, {} Channel is:{}, for msg:{}", future.channel(),
                      future.isCancelled(), future.isDone(), future.isSuccess(), pyChannel, msg,
                      future.cause());
              msg.getCallback()
                  .fail(new DisconnectionException("channel=" + pyChannel + " for msg=" + msg));
            }
          }
        });
      }
    };
  }

  @Override
  public void flush() {
    Channel channel = pyChannel.get();
    if (channel != null) {
      logger.debug("write and flush msg to channel: {}", pyChannel);
      channel.flush();
    }
  }

  @Override
  public void writeAndFlush(Message msg) {
    pyChannel.setLastIoTime(System.currentTimeMillis());
    Channel channel = pyChannel.get();
    if (channel == null) {
      logger.debug("there is no channel={}, when writing and flushing, msg={}", pyChannel, msg);
      reconnectionHandler.reconnect(buildConnectionRequest(msg, pyChannel));
      return;
    }

    logger.info("write and flush msg to channel={}, msg={}", pyChannel, msg);
    channel.writeAndFlush(msg).addListener(new GenericFutureListener<ChannelFuture>() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
          logger.warn("send failure: {}, {}, {}, {} Channel is:{}, for msg:{}", future.channel(),
              future.isCancelled(), future.isDone(), future.isSuccess(), pyChannel, msg,
              future.cause());
          msg.getCallback()
              .fail(new DisconnectionException("channel=" + pyChannel + " for msg=" + msg));
        }
      }
    });
  }

  @Override
  public String toString() {
    return "PYConnectionImpl{" + "pyChannel=" + pyChannel + '}';
  }
}
