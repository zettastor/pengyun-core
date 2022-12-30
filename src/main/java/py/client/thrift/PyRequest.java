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
