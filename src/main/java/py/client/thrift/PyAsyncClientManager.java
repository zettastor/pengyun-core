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
