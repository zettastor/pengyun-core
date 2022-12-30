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

package py.client.thrift;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.async.TAsyncMethodCall;
import org.apache.thrift.protocol.TProtocolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PyAsyncClientManager extends TAsyncClientManager {
  private static final int DEFAULT_CACHE_COUNT_FOR_CLIENT = 96;
  private static Logger logger = LoggerFactory.getLogger(PyAsyncClientManager.class);
  private final PyReflectionHelper asyncMethodCallHelper;
  private NiftyClientChannel channel;
  private String callerClassName;
  private BlockingQueue<Object> clientCaches;

  public PyAsyncClientManager(NiftyClientChannel channel, PyReflectionHelper helper)
      throws IOException {
    this.channel = channel;
    this.asyncMethodCallHelper = helper;
    this.clientCaches = new ArrayBlockingQueue<Object>(DEFAULT_CACHE_COUNT_FOR_CLIENT);

    super.stop();
  }

  public NiftyClientChannel getChannel() {
    return channel;
  }

  public void setChannel(NiftyClientChannel channel) {
    this.channel = channel;
  }

  public String getCallerClassName() {
    return callerClassName;
  }

  public void setCallerClassName(String callerClassName) {
    this.callerClassName = callerClassName;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void call(TAsyncMethodCall method) throws TException {
    try {
      asyncMethodCallHelper.getStateField().set(method,
          org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ);

      TProtocolFactory protocolFactory = (TProtocolFactory) asyncMethodCallHelper
          .getProtocolFactoryField().get(
              method);

      PyAsyncClientTransport transport = new PyAsyncClientTransport(channel);
      PyProtocolProxy proxy = (PyProtocolProxy) protocolFactory.getProtocol(transport);
      asyncMethodCallHelper.getWriteArgs().invoke(method, proxy);

      @SuppressWarnings("unchecked")
      PyResponseListener listener = new PyResponseListener(
          (AsyncMethodCallback) asyncMethodCallHelper
              .getCallbackField().get(method), method);
      listener.setFramedBufferField(asyncMethodCallHelper.getFrameBufferField());

      long timeout = asyncMethodCallHelper.getTimeoutField().getLong(method.getClient());

      transport.writeAndFlush(proxy.getSeqId(), listener, timeout);

    } catch (SecurityException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e) {
      logger.error("Invoking method {} failed,exception: {}", method.getClass().getName(), e);
      throw new TException(e);
    } finally {
      try {
        asyncMethodCallHelper.getCurrentMethod().set(method.getClient(), null);
      } catch (IllegalArgumentException | IllegalAccessException e) {
        logger.warn("catch an exception when set the methed feild ", e);
      }
    }
  }

  public void close() {
    if (channel != null) {
      channel.close();
    }
  }

  public Object get() {
    return clientCaches.poll();
  }

  public boolean recycle(Object c) {
    if (!clientCaches.offer(c)) {
      logger.warn("can not add the client={} to cache queue", c);
      return false;
    } else {
      return true;
    }
  }
}
