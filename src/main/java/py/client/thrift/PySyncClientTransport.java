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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.util.concurrent.SettableFuture;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.client.GenericProxyFactory;

public class PySyncClientTransport extends TTransport implements NiftyClientChannel.Listener {
  private static final Logger logger = LoggerFactory.getLogger(PySyncClientTransport.class);
  private static final int PACKAGE_HEAD_LENGTH = 1024;
  private static String metricPrefix = "Client-Sync";
  private final ThriftChannelBufferOutputTransport requestBufferTransport;
  private final ThriftChannelBufferInputTransport responseBufferTransport;
  private PyAsyncClientManager manager;
  private SettableFuture<ChannelBuffer> response;
  private long timeoutMs;
  private String callerClassName;
  private Object clientObject = null;
  private PyReflectionHelper pyReflectionHelper = null;
  private PyProtocolProxy proxy;
  private int maxNetworkFrameSize;

  public PySyncClientTransport(PyAsyncClientManager manager, long timeoutMs,
      int maxNetworkFrameSize) {
    this.manager = manager;
    this.requestBufferTransport = new ThriftChannelBufferOutputTransport();
    this.responseBufferTransport = new ThriftChannelBufferInputTransport(ChannelBuffers.buffer(0));
    this.timeoutMs = timeoutMs;
    this.maxNetworkFrameSize = maxNetworkFrameSize;
  }

  public void setProxy(PyProtocolProxy proxy) {
    this.proxy = proxy;
  }

  public String formatMetricName(String className, String methodName) {
    return String.format("[%s]%s_%s", metricPrefix, className, methodName);
  }

  @Override
  public boolean isOpen() {
    NiftyClientChannel niftyClientChannel = manager.getChannel();
    if (niftyClientChannel == null) {
      return false;
    }

    return niftyClientChannel.getNettyChannel().isOpen();
  }

  public void setClientObject(Object clientObject) {
    this.clientObject = clientObject;
  }

  public void setPyReflectionHelper(PyReflectionHelper pyReflectionHelper) {
    this.pyReflectionHelper = pyReflectionHelper;
  }

  @Override
  public void open() throws TTransportException {
    if (!isOpen()) {
      throw new IllegalStateException(
          "TNiftyClientChannelTransport requires an already-opened channel");
    }
  }

  @Override
  public void close() {
    NiftyClientChannel niftyClientChannel = manager.getChannel();
    if (niftyClientChannel == null) {
      return;
    }

    niftyClientChannel.getNettyChannel().close();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public int read(byte[] buf, int off, int len) throws TTransportException {
    if (!responseBufferTransport.isReadable()) {
      try {
        ChannelBuffer reponseBuffer = response.get(timeoutMs * 2, TimeUnit.MILLISECONDS);
        if (reponseBuffer == null) {
          throw new TTransportException("wait for response timeout: " + timeoutMs);
        }

        checkState(reponseBuffer.readable(), "Received an empty response");

        responseBufferTransport.setInputBuffer(reponseBuffer);

        String clientName = clientObject.getClass().getName();
        if (clientName.contains("javassist")) {
          GenericProxyFactory.JavaAssitInterceptor interceptor;
          Object delegateObj = null;
          Field[] fieldsFromMyself = clientObject.getClass().getDeclaredFields();
          for (Field f : fieldsFromMyself) {
            if (f.getName().equals("handler")) {
              f.setAccessible(true);
              interceptor = (GenericProxyFactory.JavaAssitInterceptor) f.get(clientObject);
              delegateObj = interceptor.getDelegate();
              break;
            }
          }

          List<Field> fields = IntrospectionUtil.getAllFields(delegateObj.getClass());
          for (Field f : fields) {
            if (f.getName().equals("seqid_")) {
              f.setAccessible(true);
              f.set(delegateObj, 0);
            }
          }
        } else {
          pyReflectionHelper.getSeqidField().set(clientObject, 0);
        }
      } catch (InterruptedException e) {
        logger.error("Caught an exception", e);
        Thread.currentThread().interrupt();
        throw new TTransportException(e);
      } catch (Exception e) {
        logger.info("Caught an exception", e);
        Throwable throwable = e.getCause();
        if (throwable instanceof TTransportException) {
          throw (TTransportException) throwable;
        } else {
          throw new TTransportException(TTransportException.UNKNOWN, throwable);
        }
      }
    }

    return responseBufferTransport.read(buf, off, len);
  }

  @Override
  public void write(byte[] buf, int off, int len) throws TTransportException {
    if (requestBufferTransport.getOutputBuffer().writerIndex() == 0) {
      requestBufferTransport.getOutputBuffer()
          .writerIndex(FramedClientConnector.LENGTH_FIELD_LENGTH);
    }

    requestBufferTransport.write(buf, off, len);
  }

  @Override
  public void flush() throws TTransportException {
    try {
      ChannelBuffer outputBuffer = requestBufferTransport.getOutputBuffer();

      int bodySize = outputBuffer.readableBytes() - FramedClientConnector.LENGTH_FIELD_LENGTH;
      if (bodySize > (maxNetworkFrameSize + PACKAGE_HEAD_LENGTH)) {
        throw new TTransportException(TTransportException.UNKNOWN, new TooLongFrameException(
            String.format(
                "Frame size exceeded on encode: frame was %d bytes, maximum allowed is %d bytes",
                bodySize, maxNetworkFrameSize + FramedClientConnector.LENGTH_FIELD_LENGTH + 1024)));
      }
      requestBufferTransport.getHeader().writeInt(bodySize);

      response = SettableFuture.create();

      manager.getChannel()
          .sendAsynchronousRequest(proxy.getSeqId(), outputBuffer.duplicate(), false, this,
              timeoutMs);
    } catch (TTransportException tt) {
      logger.error("write and flush failure:", tt);
      throw tt;
    } catch (TException e) {
      logger.error("write and flush failure", e);
      throw new TTransportException(e);
    } finally {
      requestBufferTransport.resetOutputBuffer();
    }
  }

  @Override
  public void onRequestSent() {
  }

  @Override
  public void onResponseReceived(ChannelBuffer response) {
    this.response.set(response);
  }

  @Override
  public void onChannelError(TException cause) {
    response.setException(cause);
  }

  public long getTimeoutMs() {
    return this.timeoutMs;
  }

  public void setTimeoutMs(long timeoutMs) {
    this.timeoutMs = timeoutMs;
  }

  public String getCallerClassName() {
    return callerClassName;
  }

  public void setCallerClassName(String callerClassName) {
    this.callerClassName = callerClassName;
  }

}
