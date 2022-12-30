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
