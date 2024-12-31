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

import java.util.concurrent.atomic.AtomicBoolean;
import org.jboss.netty.handler.timeout.TimeoutException;
import org.jboss.netty.util.Timeout;
import py.client.thrift.RequestChannel.Listener;

public class PyRequest {
  private final Listener listener;
  private final long expiredTimeMs;
  private final long timeoutMs;
  private final int requestSize;
  private Timeout sendTimeout;
  private Timeout receiveTimeout;
  private int requestId;
  private AtomicBoolean hasMethodCalled;

  public PyRequest(Listener listener, long timeoutMs, int requestSize) {
    this.listener = listener;
    this.expiredTimeMs = System.currentTimeMillis() + timeoutMs;
    this.timeoutMs = timeoutMs;
    this.hasMethodCalled = new AtomicBoolean(false);
    this.requestSize = requestSize;
  }

  public int getRequestSize() {
    return requestSize;
  }

  public boolean setListenerCalledAndReturnPreviousStatus() {
    return this.hasMethodCalled.getAndSet(true);
  }

  public Listener getListener() {
    return listener;
  }

  public Timeout getReceiveTimeout() {
    return receiveTimeout;
  }

  public void setReceiveTimeout(Timeout receiveTimeout) {
    this.receiveTimeout = receiveTimeout;
  }

  public Timeout getSendTimeout() {
    return sendTimeout;
  }

  public void setSendTimeout(Timeout sendTimeout) {
    this.sendTimeout = sendTimeout;
  }

  public int getRequestId() {
    return requestId;
  }

  public void setRequestId(int requestId) {
    this.requestId = requestId;
  }

  public long getRestTimeMs() throws TimeoutException {
    long restTime = expiredTimeMs - System.currentTimeMillis();
    if (restTime <= 0) {
      throw new TimeoutException("timeout " + timeoutMs + " has used up");
    }
    return restTime;
  }

  public long getTimeoutMs() {
    return timeoutMs;
  }

  @Override
  public String toString() {
    return "PyRequest [listener=" + listener + ", expiredTimeMs=" + expiredTimeMs + ", timeoutMs="
        + timeoutMs
        + ", sendTimeout=" + sendTimeout + ", receiveTimeout=" + receiveTimeout + ", requestId="
        + requestId + ", requestSize=" + requestSize + ", hasMethodCalled=" + hasMethodCalled + "]";
  }

}
