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
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Function;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.netty.core.Protocol;
import py.netty.core.ProtocolFactory;
import py.netty.exception.AbstractNettyException;
import py.netty.exception.GenericNettyException;
import py.netty.exception.InvalidProtocolException;
import py.netty.exception.NotSupportedException;

public class ProtocolBufProtocolFactory extends ProtocolFactory {
  private static final Logger logger = LoggerFactory.getLogger(ProtocolBufProtocolFactory.class);
  protected Method[] methods;
  protected Class<?>[] responseClazz;
  protected Class<?>[] requestClazz;

  protected Method[] parseFromOfResponses;
  protected Method[] parseFromOfRequests;
  protected ThreadLocal<byte[]> threadLocal = new ThreadLocal<>();

  protected ProtocolBufProtocolFactory(Class<?> serviceInterface,
      MethodTypeInterface[] methodTypeInterface,
      Function<Integer, MethodTypeInterface> getMethodTypeInterface)
      throws InvalidProtocolException {
    this.getMethodTypeInterface = getMethodTypeInterface;
    methods = new Method[Byte.MAX_VALUE];
    responseClazz = new Class<?>[Byte.MAX_VALUE];
    requestClazz = new Class<?>[Byte.MAX_VALUE];
    parseFromOfResponses = new Method[Byte.MAX_VALUE];
    parseFromOfRequests = new Method[Byte.MAX_VALUE];

    for (MethodTypeInterface typeInterface : methodTypeInterface) {
      if (!typeInterface.hasDefineMethod()) {
        continue;
      }

      Method method = findMethod(serviceInterface, typeInterface.name());
      if (method == null) {
        throw new NotImplementedException("not implements method: " + typeInterface);
      }

      int type = typeInterface.getValue();
      methods[type] = method;

      responseClazz[type] = getResponseClass(method);
      if (typeInterface.hasDefineDecodeMethod()) {
        parseFromOfResponses[type] = getParseFromMethod(responseClazz[type]);

        requestClazz[type] = getRequestClass(method);
        parseFromOfRequests[type] = getParseFromMethod(requestClazz[type]);
      }
    }
  }

  public static Method findMethod(Class<?> serviceInterface, String methodName) {
    for (Method method : serviceInterface.getMethods()) {
      if (0 == method.getName().compareToIgnoreCase(methodName)) {
        return method;
      }
    }
    return null;
  }

  public static ProtocolFactory create(Class<?> service) throws InvalidProtocolException {
    return create(service, MethodType.values(), integer -> MethodType.findByValue(integer));
  }

  public static ProtocolFactory create(Class<?> service,
      MethodTypeInterface[] methodTypeInterface,
      Function<Integer, MethodTypeInterface> getMethodTypeInterface)
      throws InvalidProtocolException {
    return new ProtocolBufProtocolFactory(service, methodTypeInterface, getMethodTypeInterface);
  }

  public Class<?> getResponseClass(Method method) throws InvalidProtocolException {
    Type[] parameters = method.getGenericParameterTypes();
    Type callback;
    if (parameters.length == 2) {
      callback = parameters[1];
    } else if (parameters.length == 1) {
      callback = parameters[0];
    } else {
      throw new InvalidProtocolException("can not get response class for method: " + method);
    }

    Type[] parameter = ((ParameterizedType) callback).getActualTypeArguments();
    if (parameter == null || parameter.length == 0) {
      throw new InvalidProtocolException("there is no such call back method: " + callback);
    }
    return (Class<?>) parameter[parameter.length - 1];
  }

  public Class<?> getRequestClass(Method method) throws InvalidProtocolException {
    Class<?>[] parameters = method.getParameterTypes();
    if (parameters.length == 2) {
      return parameters[0];
    } else if (parameters.length == 1) {
      return null;
    } else {
      throw new InvalidProtocolException("can not get request class for method: " + method);
    }
  }

  public Method getParseFromMethod(Class<?> classObject) throws InvalidProtocolException {
    try {
      return classObject.getMethod("parseFrom", CodedInputStream.class);
    } catch (Exception e) {
      try {
        return classObject.getMethod("parseFrom", Message.class, CodedInputStream.class);
      } catch (NoSuchMethodException | SecurityException e1) {
        logger.error("can't parse from method for {}", classObject, e1);
        throw new InvalidProtocolException("cause: " + e1);
      }
    }
  }

  public byte[] getBufferFromLocalCache(int expectedSize) {
    byte[] data = threadLocal.get();
    if (data == null) {
      data = new byte[expectedSize];
      threadLocal.set(data);
    } else {
      if (data.length < expectedSize) {
        data = new byte[expectedSize];
        threadLocal.set(data);
      }
    }

    return data;
  }

  @Override
  public Protocol getProtocol() {
    return new DefaultProtocolBufProtocol();
  }

  class DefaultProtocolBufProtocol implements Protocol {
    private DefaultProtocolBufProtocol() {
    }

    @Override
    public ByteBuf encodeResponse(Header header, AbstractMessageLite msg)
        throws InvalidProtocolException {
      if (msg == null) {
        ByteBuf metadata = allocator.buffer(Header.headerLength());
        header.toBuffer(metadata);
        MethodTypeInterface typeInterface = getMethodTypeInterface
            .apply((int) header.getMethodType());
        Validate.isTrue(typeInterface == null || !typeInterface.requestCarryData());
        return metadata;
      }

      try {
        int metadataLength = msg.getSerializedSize();
        ByteBuf metadata = allocator.buffer(Header.headerLength() + metadataLength);

        MethodTypeInterface typeInterface = getMethodTypeInterface
            .apply((int) header.getMethodType());
        if (typeInterface != null && typeInterface.responseCarryData()) {
          MessageCarryDataInterface response = (MessageCarryDataInterface) msg;
          header.setDataLength(response.getDataLength());
        }

        header.setMetadataLength(metadataLength);

        header.toBuffer(metadata);
        byte[] arrayData = getBufferFromLocalCache(metadataLength);
        try {
          msg.writeTo(CodedOutputStream.newInstance(arrayData, 0, metadataLength));
          metadata.writeBytes(arrayData, 0, metadataLength);
        } catch (IOException e) {
          metadata.release();
          throw new InvalidProtocolException(
              "can not write to header: " + header + " exception: " + e);
        }

        if (typeInterface != null && typeInterface.responseCarryData()) {
          MessageCarryDataInterface response = (MessageCarryDataInterface) msg;
          ByteBuf data = response.getData();
          metadata = (data == null ? metadata : Unpooled.wrappedBuffer(metadata, data));
        }

        return metadata;
      } finally {
        logger.info("nothing need to do here");
      }
    }

    @Override
    public ByteBuf encodeRequest(Header header, AbstractMessageLite msg)
        throws InvalidProtocolException {
      try {
        int metadataLength = header.getMetadataLength();
        ByteBuf metadata = allocator.buffer(Header.headerLength() + metadataLength);
        header.toBuffer(metadata);

        if (msg != null) {
          byte[] arrayData = getBufferFromLocalCache(metadataLength);
          try {
            msg.writeTo(CodedOutputStream.newInstance(arrayData, 0, metadataLength));
            metadata.writeBytes(arrayData, 0, metadataLength);
          } catch (IOException e) {
            metadata.release();
            throw new InvalidProtocolException("can not write to byte buf: " + e);
          }
        }

        return metadata;
      } finally {
        logger.info("nothing need to do here");
      }
    }

    @Override
    public Object decodeRequest(Message msg) throws InvalidProtocolException {
      Header header = msg.getHeader();
      if (header.getLength() == 0) {
        Validate.isTrue(msg.getBuffer() == null);
        return null;
      }

      try {
        byte[] metadata = getBufferFromLocalCache(header.getMetadataLength());
        msg.getBuffer().readBytes(metadata, 0, header.getMetadataLength());
        CodedInputStream inputStream = CodedInputStream
            .newInstance(metadata, 0, header.getMetadataLength());
        MethodTypeInterface typeInterface = getMethodTypeInterface
            .apply((int) header.getMethodType());
        if (typeInterface != null && typeInterface.requestCarryData()) {
          return parseFromOfRequests[header.getMethodType()].invoke(null, msg, inputStream);
        } else {
          return parseFromOfRequests[header.getMethodType()].invoke(null, inputStream);
        }
      } catch (Throwable t) {
        logger.error("decode request:{} failed", header.getMethodType(), t);
        throw new InvalidProtocolException(
            "can not parse the request class: " + msg + ", exception" + t);
      } finally {
        logger.info("nothing need to do here");
      }
    }

    @Override
    public Object decodeResponse(Message msg) throws InvalidProtocolException {
      Header header = msg.getHeader();
      if (header.getLength() == 0) {
        Validate.isTrue(msg.getBuffer() == null);
        return null;
      }

      try {
        byte[] metadata = getBufferFromLocalCache(header.getMetadataLength());
        msg.getBuffer().readBytes(metadata, 0, header.getMetadataLength());
        CodedInputStream inputStream = CodedInputStream
            .newInstance(metadata, 0, header.getMetadataLength());
        MethodTypeInterface typeInterface = getMethodTypeInterface
            .apply((int) header.getMethodType());
        if (typeInterface != null && typeInterface.responseCarryData()) {
          return parseFromOfResponses[header.getMethodType()].invoke(null, msg, inputStream);
        } else {
          return parseFromOfResponses[header.getMethodType()].invoke(null, inputStream);
        }
      } catch (Throwable t) {
        logger.error("decode response:{} failed", header.getMethodType(), t);
        throw new InvalidProtocolException(
            "server can't invoke method: " + header + ", exception" + t);
      } finally {
        logger.info("nothing need to do here");
      }
    }

    @Override
    public AbstractNettyException decodeException(ByteBuf buffer) throws InvalidProtocolException {
      try {
        return AbstractNettyException.parse(buffer);
      } catch (GenericNettyException e) {
        return e;
      }
    }

    public ByteBuf encodeException(long requestId, AbstractNettyException e) {
      Header header = new Header(Header.INVALID_METHOD_TYPE, e.getSize(), 0, requestId);
      ByteBuf byteBuf = allocator.buffer(Header.headerLength() + header.getLength());
      header.toBuffer(byteBuf);
      e.toBuffer(byteBuf);
      Validate.isTrue(byteBuf.readableBytes() == Header.headerLength() + header.getLength());
      return byteBuf;
    }

    @Override
    public Method getMethod(int methodType) throws NotSupportedException {
      if (methodType >= methods.length) {
        throw new NotSupportedException(methodType);
      }

      if (methods[methodType] == null) {
        throw new NotSupportedException(methodType);
      }

      return methods[methodType];
    }
  }
}
