/*
 * Copyright (C) 2012-2013 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package py.client.thrift;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.commons.lang.Validate;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioSocketChannel;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.jboss.netty.handler.timeout.TimeoutException;
import org.jboss.netty.handler.timeout.WriteTimeoutException;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.rpc.share.TransDuplexProtocolFactory;

@ThreadSafe
public abstract class PyAbstractClientChannel extends SimpleChannelHandler implements
    NiftyClientChannel {
  private static final Logger logger = LoggerFactory.getLogger(PyAbstractClientChannel.class);
  private static final int MAX_PENDING_CAPACITY = 200 * 1024 * 1024;
  private static final int MAX_PENDING_REQEUST = 2000;
  private final Channel nettyChannel;

  private final ConcurrentHashMap<Integer, PyRequest> requestMap = new ConcurrentHashMap<>();

  private final Semaphore counterSemaphore;
  private final Semaphore capacitySemaphore;
  private final int totalCapacity;

  private final Timer timer;

  private final TransDuplexProtocolFactory protocolFactory;
  private final int maxPendingCapacity;

  protected PyAbstractClientChannel(Channel nettyChannel, Timer timer,
      TransDuplexProtocolFactory protocolFactory) {
    this(nettyChannel, timer, protocolFactory, MAX_PENDING_REQEUST, MAX_PENDING_CAPACITY);
  }

  protected PyAbstractClientChannel(Channel nettyChannel, Timer timer,
      TransDuplexProtocolFactory protocolFactory,
      int maxPendingReqeust, int maxPendingCapacity) {
    this.nettyChannel = nettyChannel;
    this.timer = timer;
    this.protocolFactory = protocolFactory;
    this.counterSemaphore = new Semaphore(maxPendingReqeust);
    this.capacitySemaphore = new Semaphore(maxPendingCapacity);
    this.totalCapacity = maxPendingCapacity;
    this.maxPendingCapacity = maxPendingCapacity;
  }

  @Override
  public Channel getNettyChannel() {
    return nettyChannel;
  }

  @Override
  public TransDuplexProtocolFactory getProtocolFactory() {
    return protocolFactory;
  }

  protected abstract ChannelBuffer extractResponse(Object message) throws TTransportException;

  protected int extractSequenceId(ChannelBuffer messageBuffer) throws TTransportException {
    TMessage message = null;
    try {
      messageBuffer.markReaderIndex();
      TTransport inputTransport = new ThriftChannelBufferInputTransport(messageBuffer);
      TProtocol inputProtocol = getProtocolFactory().getInputProtocolFactory()
          .getProtocol(inputTransport);

      message = inputProtocol.readMessageBegin();
      messageBuffer.resetReaderIndex();
      return message.seqid;
    } catch (Throwable t) {
      throw new TTransportException("Could not find sequenceId in Thrift message: " + message);
    }
  }

  protected abstract ChannelFuture writeRequest(ChannelBuffer request);

  public void close() {
    getNettyChannel().close();
  }

  @Override
  public void executeInIoThread(Runnable runnable) {
    NioSocketChannel nioSocketChannel = (NioSocketChannel) getNettyChannel();
    nioSocketChannel.getWorker().executeInIoThread(runnable, true);
  }

  @Override
  public void sendAsynchronousRequest(final int seqId, final ChannelBuffer message,
      final boolean oneway,
      final Listener listener, long timeoutMs) throws TException {
    boolean acquired = false;

    try {
      long startTime = System.currentTimeMillis();
      acquired = counterSemaphore.tryAcquire(timeoutMs / 2, TimeUnit.MILLISECONDS);
      timeoutMs = timeoutMs - (System.currentTimeMillis() - startTime);
    } catch (InterruptedException e) {
      logger.error("caught an exception", e);
    }

    if (!acquired) {
      throw new TTransportException(TTransportException.TIMED_OUT, "send time out:" + timeoutMs
          + "pending request: " + requestMap.size() + " Message: " + message
          + " the connected server: "
          + nettyChannel.getRemoteAddress());
    }

    try {
      long startTime = System.currentTimeMillis();
      acquired = capacitySemaphore
          .tryAcquire(message.capacity(), timeoutMs / 2, TimeUnit.MILLISECONDS);
      timeoutMs = timeoutMs - (System.currentTimeMillis() - startTime);
    } catch (InterruptedException e) {
      logger.error("capacity:{} has used up, caught an exception", maxPendingCapacity, e);
    }

    if (capacitySemaphore.availablePermits() < maxPendingCapacity / 5 * 4) {
      logger.warn("has used up capacity: {}", maxPendingCapacity);
    }

    if (!acquired) {
      counterSemaphore.release();
      throw new TTransportException(
          "there is no capacity, total capacity: " + totalCapacity + ", leftCapacity: "
              + capacitySemaphore.availablePermits() + ", current size: " + message.capacity()
              + ", counter left: " + counterSemaphore.availablePermits());
    }

    if (!nettyChannel.isConnected()) {
      counterSemaphore.release();
      capacitySemaphore.release(message.capacity());
      throw new TTransportException(TTransportException.NOT_OPEN,
          "Can't send the request out because the underlying socket is disconnected. Message: "
              + message
              + " the connected server is " + nettyChannel.getRemoteAddress());
    }

    try {
      final PyRequest request = makeRequest(seqId, listener, timeoutMs, message.capacity());

      executeInIoThread(new Runnable() {
        @Override
        public void run() {
          try {
            ChannelFuture sendFuture = writeRequest(message);

            queueSendTimeout(request);
            sendFuture.addListener(new ChannelFutureListener() {
              @Override
              public void operationComplete(ChannelFuture future) throws Exception {
                messageSent(future, request, oneway);
              }
            });
          } catch (Throwable t) {
            logger.warn("Failed to send a message {} to the remote server {}, sequenceId: {}",
                nettyChannel.getRemoteAddress(), message, seqId, t);

            onRequestError(request, wrapException(t));
          }
        }
      });
    } catch (Exception e) {
      counterSemaphore.release();
      capacitySemaphore.release(message.capacity());
      logger.error("caught an exception", e);
    }
  }

  private void messageSent(ChannelFuture future, PyRequest request, boolean oneway) {
    if (future.isSuccess()) {
      cancelRequestTimeouts(request);
      try {
        request.getListener().onRequestSent();
      } catch (Exception e) {
        logger.error("messageSent, request: {} to server: {}", request,
            nettyChannel.getRemoteAddress(), e);
      }

      if (oneway) {
        logger.info("there should be no one way tansport: request: " + request);
        cancelRequestTimeouts(request);
      } else {
        try {
          queueReceiveTimeout(request);
        } catch (Exception e) {
          onRequestError(request, wrapException(e));
        }
      }
    } else {
      logger.error("fails to send request: {} to server: {}", request,
          nettyChannel.getRemoteAddress(),
          future.getCause());
      onRequestError(request, new TTransportException("Sending request failed", future.getCause()));
    }
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
    try {
      ChannelBuffer response = extractResponse(e.getMessage());
      if (response != null) {
        int sequenceId = extractSequenceId(response);
        PyRequest request = requestMap.remove(sequenceId);
        if (request == null) {
          logger.debug(" there is no request cooresponding to the sequence id : {} ", sequenceId);
        } else {
          cancelRequestTimeouts(request);
          if (!request.setListenerCalledAndReturnPreviousStatus()) {
            counterSemaphore.release();
            capacitySemaphore.release(request.getRequestSize());
            try {
              request.getListener().onResponseReceived(response);
            } catch (Exception e1) {
              logger.error("messageReceived, request: {} to server: {}", request,
                  nettyChannel.getRemoteAddress(), e1);
            }
          }
        }
      } else {
        logger.error("message received, but repsonse is null");
        ctx.sendUpstream(e);
      }
    } catch (Throwable t) {
      logger.error("messageReceived, caught an exception", t);
      Validate.isTrue(false);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent event) throws Exception {
    Throwable t = event.getCause();
    onError(t);
    if (!(t instanceof RejectedExecutionException)) {
      logger.error("network: {}, caught an exception", ctx.getChannel().getRemoteAddress(), t);
      ctx.getChannel().close();
    } else {
      logger.error("exceptionCaught: {}", ctx.getChannel().getRemoteAddress(), t);
    }
  }

  private PyRequest makeRequest(int sequenceId, Listener listener, long timeoutMs,
      int requestSize) {
    PyRequest request = new PyRequest(listener, timeoutMs, requestSize);
    request.setRequestId(sequenceId);
    PyRequest existing = requestMap.put(sequenceId, request);
    if (existing != null) {
      logger.error("existing request: {}, request: {}", existing, request);
    }

    return request;
  }

  private void cancelRequestTimeouts(PyRequest request) {
    Timeout sendTimeout = request.getSendTimeout();
    if (sendTimeout != null && !sendTimeout.isCancelled()) {
      sendTimeout.cancel();
    }

    Timeout receiveTimeout = request.getReceiveTimeout();
    if (receiveTimeout != null && !receiveTimeout.isCancelled()) {
      receiveTimeout.cancel();
    }
  }

  @Override
  public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    onError(new TTransportException(
        "Client was disconnected by server:" + nettyChannel.getRemoteAddress()));
  }

  protected void onRequestError(PyRequest request, Throwable t) {
    PyRequest existing = requestMap.remove(request.getRequestId());
    if (existing == null) {
      logger.debug("there is no request cooresponding to the sequence id : {} when meeting error ",
          existing);
    } else {
      cancelRequestTimeouts(request);
      if (!request.setListenerCalledAndReturnPreviousStatus()) {
        counterSemaphore.release();
        capacitySemaphore.release(request.getRequestSize());
        request.getListener().onChannelError(wrapException(t));
      }
    }
  }

  protected void onError(Throwable t) {
    try {
      TException wrappedException = wrapException(t);
      for (Entry<Integer, PyRequest> entry : requestMap.entrySet()) {
        PyRequest request = entry.getValue();
        cancelRequestTimeouts(request);
        if (!request.setListenerCalledAndReturnPreviousStatus()) {
          counterSemaphore.release();
          capacitySemaphore.release(request.getRequestSize());
          request.getListener().onChannelError(wrappedException);
        }
        requestMap.remove(entry.getKey());
      }
    } catch (Exception e) {
      logger.error("onError, caught an exception", e);
    }
  }

  protected TException wrapException(Throwable t) {
    if (t instanceof TException) {
      return (TException) t;
    } else {
      return new TTransportException(t);
    }
  }

  private void onSendTimeoutFired(PyRequest request) {
    WriteTimeoutException timeoutException = new WriteTimeoutException(
        "Timed out waiting to send data to server: "
            + nettyChannel.getRemoteAddress() + ", request: " + request);
    onRequestError(request,
        new TTransportException(TTransportException.TIMED_OUT, timeoutException));
  }

  private void onReceiveTimeoutFired(PyRequest request) {
    ReadTimeoutException timeoutException = new ReadTimeoutException("Timed out waiting to server: "
        + nettyChannel.getRemoteAddress() + " receive response, request: " + request);
    onRequestError(request,
        new TTransportException(TTransportException.TIMED_OUT, timeoutException));
  }

  private void queueSendTimeout(final PyRequest request) throws TTransportException {
    try {
      long timeout = request.getTimeoutMs();
      TimerTask sendTimeoutTask = new IoThreadBoundTimerTask(this, new TimerTask() {
        @Override
        public void run(Timeout timeout) {
          onSendTimeoutFired(request);
        }
      });

      Timeout sendTimeout = timer.newTimeout(sendTimeoutTask, timeout, TimeUnit.MILLISECONDS);
      request.setSendTimeout(sendTimeout);
    } catch (TimeoutException e) {
      logger.info("it takes too long time to send request, request: {}, to server: {}", request,
          nettyChannel.getRemoteAddress());
      throw new TTransportException(TTransportException.TIMED_OUT,
          "the timeout of sending data, time:"
              + request.getTimeoutMs());
    } catch (Exception e) {
      logger.error("can not schedule send timeout, request: {}, to server: {}", request,
          nettyChannel.getRemoteAddress());
      throw new TTransportException(
          "Unable to wait sending to server: " + nettyChannel.getRemoteAddress());
    }
  }

  private void queueReceiveTimeout(final PyRequest request) throws TTransportException {
    try {
      long timeout = request.getTimeoutMs();
      TimerTask receiveTimeoutTask = new IoThreadBoundTimerTask(this, new TimerTask() {
        @Override
        public void run(Timeout timeout) {
          onReceiveTimeoutFired(request);
        }
      });

      Timeout receiveTimeout = timer.newTimeout(receiveTimeoutTask, timeout, TimeUnit.MILLISECONDS);
      request.setReceiveTimeout(receiveTimeout);
    } catch (TimeoutException e) {
      logger.info("it takes too long time to wait response, request: {}, to server: {}", request,
          nettyChannel.getRemoteAddress());
      throw new TTransportException(TTransportException.TIMED_OUT,
          "the timeout of waiting response, timeout"
              + request.getTimeoutMs());
    } catch (Exception e) {
      logger.error("can not schedule receive timeout, request: {}, to server: {}", request,
          nettyChannel.getRemoteAddress());
      throw new TTransportException(
          "Unable to wait receiving from server: " + nettyChannel.getRemoteAddress());
    }
  }

  private static class IoThreadBoundTimerTask implements TimerTask {
    private final NiftyClientChannel channel;
    private final TimerTask timerTask;

    public IoThreadBoundTimerTask(NiftyClientChannel channel, TimerTask timerTask) {
      this.channel = channel;
      this.timerTask = timerTask;
    }

    @Override
    public void run(final Timeout timeout) throws Exception {
      channel.executeInIoThread(new Runnable() {
        @Override
        public void run() {
          try {
            timerTask.run(timeout);
          } catch (Exception e) {
            Channels.fireExceptionCaught(channel.getNettyChannel(), e);
          }
        }
      });
    }
  }
}
