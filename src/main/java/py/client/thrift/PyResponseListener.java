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

import java.lang.reflect.Field;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncMethodCall;
import org.jboss.netty.buffer.ChannelBuffer;

public class PyResponseListener<T> implements RequestChannel.Listener {
  private static final Logger logger = Logger.getLogger(PyResponseListener.class);
  private AsyncMethodCallback<T> callback;
  private TAsyncMethodCall<T> method;
  private Field frameBufferField;

  public PyResponseListener(AsyncMethodCallback<T> callback, TAsyncMethodCall<T> method) {
    this.callback = callback;
    this.method = method;
  }

  public void setFramedBufferField(Field frameBufferField) {
    this.frameBufferField = frameBufferField;
  }

  @Override
  public void onRequestSent() {
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onResponseReceived(ChannelBuffer responsebuffer) {
    try {
      frameBufferField.set(method, responsebuffer.toByteBuffer());
    } catch (IllegalArgumentException | IllegalAccessException e) {
      logger.error("Invoking method " + method.getClass().getName() + " failed", e);
      callback.onError(e);
      return;
    }
    callback.onComplete((T) method);
  }

  @Override
  public void onChannelError(TException requestException) {
    callback.onError(requestException);
  }

}
