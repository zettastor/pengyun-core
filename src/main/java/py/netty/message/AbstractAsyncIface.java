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

package py.netty.message;

import com.google.protobuf.AbstractMessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.connection.pool.PyConnection;
import py.netty.core.MethodCallback;
import py.netty.core.Protocol;
import py.netty.core.ProtocolFactory;
import py.netty.exception.DisconnectionException;
import py.netty.exception.InvalidProtocolException;

public abstract class AbstractAsyncIface {
  private static final Logger logger = LoggerFactory.getLogger(AbstractAsyncIface.class);
  private final PyConnection connection;
  private final Protocol protocol;

  public AbstractAsyncIface(PyConnection connection, Protocol protocol) {
    this.connection = connection;
    this.protocol = protocol;
  }

  protected Header generateHeader(byte methodType, int metadataLength, int dataLength) {
    return new Header(methodType, metadataLength, dataLength, ProtocolFactory
        .getRequestId());
  }

  protected void send(Header header, AbstractMessageLite request, MethodCallback callback,
      ByteBuf data) {
    send(header, request, callback, data, true);
  }

  protected void send(Header header, AbstractMessageLite request, MethodCallback callback,
      ByteBuf data,
      boolean bflush) {
    ByteBuf metadata = null;
    try {
      metadata = protocol.encodeRequest(header, request);
    } catch (InvalidProtocolException e) {
      logger.warn("caught an exception", e);
      if (metadata != null) {
        metadata.release();
      }
      callback.fail(e);
      return;
    }

    Validate.notNull(metadata);
    ByteBuf dataForWholeMessage = null;
    try {
      if (data != null) {
        dataForWholeMessage = Unpooled.wrappedBuffer(metadata, data);
      } else {
        dataForWholeMessage = metadata;
      }
      Validate.notNull(dataForWholeMessage);

      AtomicBoolean isResponse = new AtomicBoolean(false);
      MethodCallback<AbstractMessageLite> callbackWrapper = new
          MethodCallback<AbstractMessageLite>() {
            @Override
            public void complete(AbstractMessageLite object) {
              if (isResponse.compareAndSet(false, true)) {
                callback.complete(object);
              } else {
                logger.warn("callback already been invoked. Request:{}, isResponse:{}",
                    header.getRequestId(), isResponse);
              }
            }

            @Override
            public void fail(Exception e) {
              if (isResponse.compareAndSet(false, true)) {
                callback.fail(e);
              } else {
                logger.warn("callback already been invoked. Request:{}, isResponse:{}.",
                    header.getRequestId(), isResponse, e);
              }
            }

            @Override
            public ByteBufAllocator getAllocator() {
              return callback.getAllocator();
            }
          };
      connection.write(new SendMessage(header, dataForWholeMessage, callbackWrapper));
    } catch (Exception e) {
      logger.error("caught an exception", e);
      if (dataForWholeMessage != null) {
        dataForWholeMessage.release();
      }
      callback.fail(new DisconnectionException(e.getMessage()));
    }

  }
}
