/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package py.netty.core.twothreads;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelId;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.VoidChannelPromise;
import io.netty.channel.socket.ChannelOutputShutdownEvent;
import io.netty.channel.socket.ChannelOutputShutdownException;
import io.netty.util.DefaultAttributeMap;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.ThrowableUtil;
import io.netty.util.internal.UnstableApi;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractChannel extends DefaultAttributeMap implements Channel {
  private static final Logger logger = LoggerFactory.getLogger(AbstractChannel.class);

  private static final ClosedChannelException FLUSH0_CLOSED_CHANNEL_EXCEPTION = ThrowableUtil
      .unknownStackTrace(new ClosedChannelException(), AbstractUnsafe.class, "flush0()");
  private static final ClosedChannelException ENSURE_OPEN_CLOSED_CHANNEL_EXCEPTION = ThrowableUtil
      .unknownStackTrace(new ClosedChannelException(), AbstractUnsafe.class, "ensureOpen(...)");
  private static final ClosedChannelException CLOSE_CLOSED_CHANNEL_EXCEPTION = ThrowableUtil
      .unknownStackTrace(new ClosedChannelException(), AbstractUnsafe.class, "close(...)");
  private static final ClosedChannelException WRITE_CLOSED_CHANNEL_EXCEPTION = ThrowableUtil
      .unknownStackTrace(new ClosedChannelException(), AbstractUnsafe.class, "write(...)");
  private static final NotYetConnectedException FLUSH0_NOT_YET_CONNECTED_EXCEPTION = ThrowableUtil
      .unknownStackTrace(new NotYetConnectedException(), AbstractUnsafe.class, "flush0()");

  private final Channel parent;
  private final ChannelId id;
  private final AbstractUnsafe unsafe;
  private final DefaultChannelPipeline pipeline;
  private final VoidChannelPromise unsafeVoidPromise = new VoidChannelPromise(this, false);
  private final CloseFuture closeFuture = new CloseFuture(this);

  private volatile SocketAddress localAddress;
  private volatile SocketAddress remoteAddress;
  private volatile EventLoop eventLoop;
  private volatile boolean registered;
  private boolean closeInitiated;

  private boolean strValActive;
  private String strVal;

  protected AbstractChannel(Channel parent) {
    this.parent = parent;
    id = newId();
    unsafe = newUnsafe();
    pipeline = newChannelPipeline();
  }

  protected AbstractChannel(Channel parent, ChannelId id) {
    this.parent = parent;
    this.id = id;
    unsafe = newUnsafe();
    pipeline = newChannelPipeline();

  }

  @Override
  public final ChannelId id() {
    return id;
  }

  protected ChannelId newId() {
    return DefaultChannelId.newInstance();
  }

  protected DefaultChannelPipeline newChannelPipeline() {
    return new DefaultChannelPipeline(this);
  }

  @Override
  public boolean isWritable() {
    ChannelOutboundBuffer buf = unsafe.getOutboundBuffer();
    return buf != null && buf.isWritable();
  }

  @Override
  public long bytesBeforeUnwritable() {
    ChannelOutboundBuffer buf = unsafe.getOutboundBuffer();
   
   
   
    return buf != null ? buf.bytesBeforeUnwritable() : 0;
  }

  @Override
  public long bytesBeforeWritable() {
    ChannelOutboundBuffer buf = unsafe.getOutboundBuffer();
   
   
   
    return buf != null ? buf.bytesBeforeWritable() : Long.MAX_VALUE;
  }

  @Override
  public Channel parent() {
    return parent;
  }

  @Override
  public ChannelPipeline pipeline() {
    return pipeline;
  }

  @Override
  public ByteBufAllocator alloc() {
    return config().getAllocator();
  }

  @Override
  public EventLoop eventLoop() {
    EventLoop eventLoop = this.eventLoop;
    if (eventLoop == null) {
      throw new IllegalStateException("channel not registered to an event loop");
    }
    return eventLoop;
  }

  @Override
  public SocketAddress localAddress() {
    SocketAddress localAddress = this.localAddress;
    if (localAddress == null) {
      try {
        this.localAddress = localAddress = unsafe().localAddress();
      } catch (Throwable t) {
       
        return null;
      }
    }
    return localAddress;
  }

  @Deprecated
  protected void invalidateLocalAddress() {
    localAddress = null;
  }

  @Override
  public SocketAddress remoteAddress() {
    SocketAddress remoteAddress = this.remoteAddress;
    if (remoteAddress == null) {
      try {
        this.remoteAddress = remoteAddress = unsafe().remoteAddress();
      } catch (Throwable t) {
       
        return null;
      }
    }
    return remoteAddress;
  }

  @Deprecated
  protected void invalidateRemoteAddress() {
    remoteAddress = null;
  }

  @Override
  public boolean isRegistered() {
    return registered;
  }

  @Override
  public ChannelFuture bind(SocketAddress localAddress) {
    return pipeline.bind(localAddress);
  }

  @Override
  public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
    return pipeline.bind(localAddress, promise);
  }

  @Override
  public ChannelFuture connect(SocketAddress remoteAddress) {
    return pipeline.connect(remoteAddress);
  }

  @Override
  public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
    return pipeline.connect(remoteAddress, localAddress);
  }

  @Override
  public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
    return pipeline.connect(remoteAddress, promise);
  }

  @Override
  public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress,
      ChannelPromise promise) {
    return pipeline.connect(remoteAddress, localAddress, promise);
  }

  @Override
  public ChannelFuture disconnect() {
    return pipeline.disconnect();
  }

  @Override
  public ChannelFuture disconnect(ChannelPromise promise) {
    return pipeline.disconnect(promise);
  }

  @Override
  public ChannelFuture close() {
    return pipeline.close();
  }

  @Override
  public ChannelFuture close(ChannelPromise promise) {
    return pipeline.close(promise);
  }

  @Override
  public ChannelFuture deregister() {
    return pipeline.deregister();
  }

  @Override
  public ChannelFuture deregister(ChannelPromise promise) {
    return pipeline.deregister(promise);
  }

  @Override
  public Channel flush() {
    pipeline.flush();
    return this;
  }

  @Override
  public Channel read() {
    pipeline.read();
    return this;
  }

  @Override
  public ChannelFuture write(Object msg) {
    return pipeline.write(msg);
  }

  @Override
  public ChannelFuture write(Object msg, ChannelPromise promise) {
    return pipeline.write(msg, promise);
  }

  @Override
  public ChannelFuture writeAndFlush(Object msg) {
    return pipeline.writeAndFlush(msg);
  }

  @Override
  public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
    return pipeline.writeAndFlush(msg, promise);
  }

  @Override
  public ChannelPromise newPromise() {
    return pipeline.newPromise();
  }

  @Override
  public ChannelProgressivePromise newProgressivePromise() {
    return pipeline.newProgressivePromise();
  }

  @Override
  public ChannelFuture newSucceededFuture() {
    return pipeline.newSucceededFuture();
  }

  @Override
  public ChannelFuture newFailedFuture(Throwable cause) {
    return pipeline.newFailedFuture(cause);
  }

  @Override
  public ChannelFuture closeFuture() {
    return closeFuture;
  }

  @Override
  public Unsafe unsafe() {
    return unsafe;
  }

  protected abstract AbstractUnsafe newUnsafe();

  @Override
  public final int hashCode() {
    return id.hashCode();
  }

  @Override
  public final boolean equals(Object o) {
    return this == o;
  }

  @Override
  public final int compareTo(Channel o) {
    if (this == o) {
      return 0;
    }

    return id().compareTo(o.id());
  }

  @Override
  public String toString() {
    boolean active = isActive();
    if (strValActive == active && strVal != null) {
      return strVal;
    }

    SocketAddress remoteAddr = remoteAddress();
    SocketAddress localAddr = localAddress();
    if (remoteAddr != null) {
      StringBuilder buf = new StringBuilder(96).append("[id: 0x").append(id.asShortText())
          .append(", L:")
          .append(localAddr).append(active ? " - " : " ! ").append("R:").append(remoteAddr)
          .append(']');
      strVal = buf.toString();
    } else if (localAddr != null) {
      StringBuilder buf = new StringBuilder(64).append("[id: 0x").append(id.asShortText())
          .append(", L:")
          .append(localAddr).append(']');
      strVal = buf.toString();
    } else {
      StringBuilder buf = new StringBuilder(16).append("[id: 0x").append(id.asShortText())
          .append(']');
      strVal = buf.toString();
    }

    strValActive = active;
    return strVal;
  }

  @Override
  public final ChannelPromise voidPromise() {
    return pipeline.voidPromise();
  }

  protected abstract boolean isCompatible(EventLoop loop);

  protected abstract SocketAddress localAddress0();

  protected abstract SocketAddress remoteAddress0();

  protected void doRegister() throws Exception {
   
  }

  protected abstract void doBind(SocketAddress localAddress) throws Exception;

  protected abstract void doDisconnect() throws Exception;

  protected abstract void doClose() throws Exception;

  @UnstableApi
  protected void doShutdownOutput() throws Exception {
    doClose();
  }

  protected void doDeregister() throws Exception {
   
  }

  protected abstract void doBeginRead() throws Exception;

  protected abstract void doWrite(ChannelOutboundBuffer in) throws Exception;

  protected Object filterOutboundMessage(Object msg) throws Exception {
    return msg;
  }

  static final class CloseFuture extends DefaultChannelPromise {
    CloseFuture(AbstractChannel ch) {
      super(ch);
    }

    @Override
    public ChannelPromise setSuccess() {
      throw new IllegalStateException();
    }

    @Override
    public ChannelPromise setFailure(Throwable cause) {
      throw new IllegalStateException();
    }

    @Override
    public boolean trySuccess() {
      throw new IllegalStateException();
    }

    @Override
    public boolean tryFailure(Throwable cause) {
      throw new IllegalStateException();
    }

    boolean setClosed() {
      return super.trySuccess();
    }
  }

  private static final class AnnotatedConnectException extends ConnectException {
    private static final long serialVersionUID = 3901958112696433556L;

    AnnotatedConnectException(ConnectException exception, SocketAddress remoteAddress) {
      super(exception.getMessage() + ": " + remoteAddress);
      initCause(exception);
      setStackTrace(exception.getStackTrace());
    }

    @Override
    public Throwable fillInStackTrace() {
      return this;
    }
  }

  private static final class AnnotatedNoRouteToHostException extends NoRouteToHostException {
    private static final long serialVersionUID = -6801433937592080623L;

    AnnotatedNoRouteToHostException(NoRouteToHostException exception, SocketAddress remoteAddress) {
      super(exception.getMessage() + ": " + remoteAddress);
      initCause(exception);
      setStackTrace(exception.getStackTrace());
    }

    @Override
    public Throwable fillInStackTrace() {
      return this;
    }
  }

  private static final class AnnotatedSocketException extends SocketException {
    private static final long serialVersionUID = 3896743275010454039L;

    AnnotatedSocketException(SocketException exception, SocketAddress remoteAddress) {
      super(exception.getMessage() + ": " + remoteAddress);
      initCause(exception);
      setStackTrace(exception.getStackTrace());
    }

    @Override
    public Throwable fillInStackTrace() {
      return this;
    }
  }

  protected abstract class AbstractUnsafe implements Unsafe {
    private volatile ChannelOutboundBuffer outboundBuffer = new ChannelOutboundBuffer(
        AbstractChannel.this);
    private RecvByteBufAllocator.Handle recvHandle;
    private boolean inFlush0;
    
    private boolean neverRegistered = true;

    private void assertEventLoop() {
      assert !registered || eventLoop.inEventLoop();
    }

    @Override
    public RecvByteBufAllocator.Handle recvBufAllocHandle() {
      if (recvHandle == null) {
        recvHandle = config().getRecvByteBufAllocator().newHandle();
      }
      return recvHandle;
    }

    ChannelOutboundBuffer getOutboundBuffer() {
      return outboundBuffer;
    }

    @Override
    public final SocketAddress localAddress() {
      return localAddress0();
    }

    @Override
    public final SocketAddress remoteAddress() {
      return remoteAddress0();
    }

    @Override
    public final void register(EventLoop eventLoop, final ChannelPromise promise) {
      if (eventLoop == null) {
        throw new NullPointerException("eventLoop");
      }
      if (isRegistered()) {
        promise.setFailure(new IllegalStateException("registered to an event loop already"));
        return;
      }
      if (!isCompatible(eventLoop)) {
        promise.setFailure(
            new IllegalStateException(
                "incompatible event loop type: " + eventLoop.getClass().getName()));
        return;
      }

      AbstractChannel.this.eventLoop = eventLoop;

      if (eventLoop.inEventLoop()) {
        register0(promise);
      } else {
        try {
          eventLoop.execute(new Runnable() {
            @Override
            public void run() {
              register0(promise);
            }
          });
        } catch (Throwable t) {
          logger.warn(
              "Force-closing a channel whose registration task was not accepted by an event "
                  + "loop: {}", this, t);
          closeForcibly();
          closeFuture.setClosed();
          safeSetFailure(promise, t);
        }
      }
    }

    private void register0(ChannelPromise promise) {
      try {
       
       
        if (!promise.setUncancellable() || !ensureOpen(promise)) {
          return;
        }
        boolean firstRegistration = neverRegistered;
        doRegister();
        neverRegistered = false;
        registered = true;

        pipeline.invokeHandlerAddedIfNeeded();

        safeSetSuccess(promise);
        pipeline.fireChannelRegistered();
       
       
        if (isActive()) {
          if (firstRegistration) {
            pipeline.fireChannelActive();
          } else if (config().isAutoRead()) {
           
           
           
           
            beginRead();
          }
        }
      } catch (Throwable t) {
       
        closeForcibly();
        closeFuture.setClosed();
        safeSetFailure(promise, t);
      }
    }

    @Override
    public final void bind(final SocketAddress localAddress, final ChannelPromise promise) {
      assertEventLoop();

      if (!promise.setUncancellable() || !ensureOpen(promise)) {
        return;
      }

      if (Boolean.TRUE.equals(config().getOption(ChannelOption.SO_BROADCAST))
          && localAddress instanceof InetSocketAddress && !((InetSocketAddress) localAddress)
          .getAddress()
          .isAnyLocalAddress() && !PlatformDependent.isWindows() && !PlatformDependent
          .maybeSuperUser()) {
       
       
        logger.warn("A non-root user can't receive a broadcast packet if the socket "
            + "is not bound to a wildcard address; binding to a non-wildcard " + "address ("
            + localAddress
            + ") anyway as requested.");
      }

      boolean wasActive = isActive();
      try {
        doBind(localAddress);
      } catch (Throwable t) {
        safeSetFailure(promise, t);
        closeIfClosed();
        return;
      }

      if (!wasActive && isActive()) {
        invokeLater(new Runnable() {
          @Override
          public void run() {
            pipeline.fireChannelActive();
          }
        });
      }

      safeSetSuccess(promise);
    }

    @Override
    public final void disconnect(final ChannelPromise promise) {
      assertEventLoop();

      if (!promise.setUncancellable()) {
        return;
      }

      boolean wasActive = isActive();
      try {
        doDisconnect();
      } catch (Throwable t) {
        safeSetFailure(promise, t);
        closeIfClosed();
        return;
      }

      if (wasActive && !isActive()) {
        invokeLater(new Runnable() {
          @Override
          public void run() {
            pipeline.fireChannelInactive();
          }
        });
      }

      safeSetSuccess(promise);
      closeIfClosed();
    }

    @Override
    public final void close(final ChannelPromise promise) {
      assertEventLoop();

      close(promise, CLOSE_CLOSED_CHANNEL_EXCEPTION, CLOSE_CLOSED_CHANNEL_EXCEPTION, false);
    }

    private void close(final ChannelPromise promise, final Throwable cause,
        final ClosedChannelException closeCause,
        final boolean notify) {
      if (!promise.setUncancellable()) {
        return;
      }

      logger.warn("closing the channel {} ", this);
      if (closeInitiated) {
        if (closeFuture.isDone()) {
         
          safeSetSuccess(promise);
        } else if (!(promise instanceof VoidChannelPromise)) {
         
         
          closeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
              promise.setSuccess();
            }
          });
        }
        return;
      }

      closeInitiated = true;

      final boolean wasActive = isActive();
      final ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
      this.outboundBuffer = null;
      Executor closeExecutor = prepareToClose();
      if (closeExecutor != null) {
        closeExecutor.execute(new Runnable() {
          @Override
          public void run() {
            try {
             
              doClose0(promise);
            } finally {
              invokeLater(new Runnable() {
                @Override
                public void run() {
                  if (outboundBuffer != null) {
                   
                    outboundBuffer.failFlushed(cause, notify);
                    outboundBuffer.close(closeCause);
                  }
                  fireChannelInactiveAndDeregister(wasActive);
                }
              });
            }
          }
        });
      } else {
        try {
         
          doClose0(promise);
        } finally {
          if (outboundBuffer != null) {
           
            outboundBuffer.failFlushed(cause, notify);
            outboundBuffer.close(closeCause);
          }
        }
        if (inFlush0) {
          invokeLater(new Runnable() {
            @Override
            public void run() {
              fireChannelInactiveAndDeregister(wasActive);
            }
          });
        } else {
          fireChannelInactiveAndDeregister(wasActive);
        }
      }
    }

    @UnstableApi
    public final void shutdownOutput(final ChannelPromise promise) {
      assertEventLoop();
      shutdownOutput(promise, null);
    }

    private void shutdownOutput(final ChannelPromise promise, Throwable cause) {
      if (!promise.setUncancellable()) {
        return;
      }

      final ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
      if (outboundBuffer == null) {
        promise.setFailure(CLOSE_CLOSED_CHANNEL_EXCEPTION);
        return;
      }
      this.outboundBuffer = null;

      final Throwable shutdownCause = cause == null
          ? new ChannelOutputShutdownException("Channel output shutdown")
          : new ChannelOutputShutdownException("Channel output shutdown", cause);
      Executor closeExecutor = prepareToClose();
      if (closeExecutor != null) {
        closeExecutor.execute(new Runnable() {
          @Override
          public void run() {
            try {
             
              doShutdownOutput();
              promise.setSuccess();
            } catch (Throwable err) {
              promise.setFailure(err);
            } finally {
              eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                  closeOutboundBufferForShutdown(pipeline, outboundBuffer, shutdownCause);
                }
              });
            }
          }
        });
      } else {
        try {
         
          doShutdownOutput();
          promise.setSuccess();
        } catch (Throwable err) {
          promise.setFailure(err);
        } finally {
          closeOutboundBufferForShutdown(pipeline, outboundBuffer, shutdownCause);
        }
      }
    }

    private void closeOutboundBufferForShutdown(ChannelPipeline pipeline,
        ChannelOutboundBuffer buffer,
        Throwable cause) {
      buffer.failFlushed(cause, false);
      buffer.close(cause, true);
      pipeline.fireUserEventTriggered(ChannelOutputShutdownEvent.INSTANCE);
    }

    private void doClose0(ChannelPromise promise) {
      try {
        doClose();
        closeFuture.setClosed();
        safeSetSuccess(promise);
      } catch (Throwable t) {
        closeFuture.setClosed();
        safeSetFailure(promise, t);
      }
    }

    private void fireChannelInactiveAndDeregister(final boolean wasActive) {
      deregister(voidPromise(), wasActive && !isActive());
    }

    @Override
    public final void closeForcibly() {
      assertEventLoop();

      try {
        doClose();
      } catch (Exception e) {
        logger.warn("Failed to close a channel.", e);
      }
    }

    @Override
    public final void deregister(final ChannelPromise promise) {
      assertEventLoop();

      deregister(promise, false);
    }

    private void deregister(final ChannelPromise promise, final boolean fireChannelInactive) {
      if (!promise.setUncancellable()) {
        return;
      }

      if (!registered) {
        safeSetSuccess(promise);
        return;
      }

      invokeLater(new Runnable() {
        @Override
        public void run() {
          try {
            doDeregister();
          } catch (Throwable t) {
            logger.warn("Unexpected exception occurred while deregistering a channel.", t);
          } finally {
            if (fireChannelInactive) {
              pipeline.fireChannelInactive();
            }
           
           
           
           
            if (registered) {
              registered = false;
              pipeline.fireChannelUnregistered();
            }
            safeSetSuccess(promise);
          }
        }
      });
    }

    @Override
    public final void beginRead() {
      assertEventLoop();

      if (!isActive()) {
        return;
      }

      try {
        doBeginRead();
      } catch (final Exception e) {
        invokeLater(new Runnable() {
          @Override
          public void run() {
            pipeline.fireExceptionCaught(e);
          }
        });
        close(voidPromise());
      }
    }

    @Override
    public final void write(Object msg, ChannelPromise promise) {
      assertEventLoop();

      ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
      if (outboundBuffer == null) {
       
       
       
       
        safeSetFailure(promise, WRITE_CLOSED_CHANNEL_EXCEPTION);
       
        ReferenceCountUtil.release(msg);
        return;
      }

      int size;
      try {
        msg = filterOutboundMessage(msg);
        size = pipeline.estimatorHandle().size(msg);
        if (size < 0) {
          size = 0;
        }
      } catch (Throwable t) {
        safeSetFailure(promise, t);
        ReferenceCountUtil.release(msg);
        return;
      }

      outboundBuffer.addMessage(msg, size, promise);
    }

    @Override
    public final void flush() {
      assertEventLoop();

      ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
      if (outboundBuffer == null) {
        return;
      }

      outboundBuffer.addFlush();
      flush0();
    }

    @SuppressWarnings("deprecation")
    protected void flush0() {
      if (inFlush0) {
       
        return;
      }

      final ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
      if (outboundBuffer == null || outboundBuffer.isEmpty()) {
        return;
      }

      inFlush0 = true;

      if (!isActive()) {
        try {
          if (isOpen()) {
            outboundBuffer.failFlushed(FLUSH0_NOT_YET_CONNECTED_EXCEPTION, true);
          } else {
           
            outboundBuffer.failFlushed(FLUSH0_CLOSED_CHANNEL_EXCEPTION, false);
          }
        } finally {
          inFlush0 = false;
        }
        return;
      }

      try {
        doWrite(outboundBuffer);
      } catch (Throwable t) {
        if (t instanceof IOException && config().isAutoClose()) {
          
          close(voidPromise(), t, FLUSH0_CLOSED_CHANNEL_EXCEPTION, false);
        } else {
          try {
            shutdownOutput(voidPromise(), t);
          } catch (Throwable t2) {
            close(voidPromise(), t2, FLUSH0_CLOSED_CHANNEL_EXCEPTION, false);
          }
        }
      } finally {
        inFlush0 = false;
      }
    }

    @Override
    public final ChannelPromise voidPromise() {
      assertEventLoop();

      return unsafeVoidPromise;
    }

    protected final boolean ensureOpen(ChannelPromise promise) {
      if (isOpen()) {
        return true;
      }

      safeSetFailure(promise, ENSURE_OPEN_CLOSED_CHANNEL_EXCEPTION);
      return false;
    }

    protected final void safeSetSuccess(ChannelPromise promise) {
      if (!(promise instanceof VoidChannelPromise) && !promise.trySuccess()) {
        logger.warn("Failed to mark a promise as success because it is done already: {}", promise);
      }
    }

    protected final void safeSetFailure(ChannelPromise promise, Throwable cause) {
      if (!(promise instanceof VoidChannelPromise) && !promise.tryFailure(cause)) {
        logger.warn("Failed to mark a promise as failure because it's done already: {}", promise,
            cause);
      }
    }

    protected final void closeIfClosed() {
      if (isOpen()) {
        return;
      }
      close(voidPromise());
    }

    private void invokeLater(Runnable task) {
      try {
       
       
       
       
       
       
       
       
       
       
       
       
       
        eventLoop().execute(task);
      } catch (RejectedExecutionException e) {
        logger.warn("Can't invoke task later as EventLoop rejected it", e);
      }
    }

    protected final Throwable annotateConnectException(Throwable cause,
        SocketAddress remoteAddress) {
      if (cause instanceof ConnectException) {
        return new AnnotatedConnectException((ConnectException) cause, remoteAddress);
      }
      if (cause instanceof NoRouteToHostException) {
        return new AnnotatedNoRouteToHostException((NoRouteToHostException) cause, remoteAddress);
      }
      if (cause instanceof SocketException) {
        return new AnnotatedSocketException((SocketException) cause, remoteAddress);
      }

      return cause;
    }

    protected Executor prepareToClose() {
      return null;
    }

    @Override
    public io.netty.channel.ChannelOutboundBuffer outboundBuffer() {
      return null;
    }
  }
}
