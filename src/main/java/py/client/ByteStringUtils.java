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

package py.client;

import com.google.protobuf.ByteString;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class ByteStringUtils {
  private static final Logger logger = LoggerFactory.getLogger(ByteStringUtils.class);

  private static Constructor<?> literalByteString;
  private static Constructor<?> boundedByteString;
  private static Field isReadOnlyFieldForByteBuffer;

  static {
    Class<?> classByteString = null;
    try {
      classByteString = Class.forName("com.google.protobuf.ByteString$LiteralByteString");
    } catch (ClassNotFoundException e) {
      logger.error("caught an exception", e);
      throw new RuntimeException("Get Sub Class of ByteString failure 1");
    }

    for (Constructor<?> constructor : classByteString.getDeclaredConstructors()) {
      if (constructor.getParameterTypes().length != 1) {
        continue;
      }
      if (constructor.getParameterTypes()[0].getSimpleName().equals(byte[].class.getSimpleName())) {
        constructor.setAccessible(true);
        literalByteString = constructor;
        break;
      }
    }

    try {
      classByteString = Class.forName("com.google.protobuf.ByteString$BoundedByteString");
    } catch (ClassNotFoundException e) {
      logger.error("caught an exception", e);
      throw new RuntimeException("Get Sub Class of ByteString failure 2");
    }

    for (Constructor<?> constructor : classByteString.getDeclaredConstructors()) {
      if (constructor.getParameterTypes().length != 3) {
        continue;
      }

      if (!constructor.getParameterTypes()[0].getSimpleName()
          .equals(byte[].class.getSimpleName())) {
        continue;
      }

      if (!constructor.getParameterTypes()[1].getSimpleName().equals(int.class.getSimpleName())) {
        continue;
      }

      if (!constructor.getParameterTypes()[2].getSimpleName().equals(int.class.getSimpleName())) {
        continue;
      }

      constructor.setAccessible(true);
      boundedByteString = constructor;
    }
    try {
      isReadOnlyFieldForByteBuffer = ByteBuffer.class.getDeclaredField("isReadOnly");
      isReadOnlyFieldForByteBuffer.setAccessible(true);
    } catch (NoSuchFieldException | SecurityException e) {
      throw new RuntimeException("ByteBuffer has no field for isReadOnly");
    }

    if (boundedByteString == null || literalByteString == null) {
      throw new RuntimeException(
          "Get Sub Class of ByteString failure 3, " + boundedByteString + ", "
              + literalByteString);
    }
  }

  public static ByteString newInstance(byte[] data) {
    Object[] intArgs = new Object[]{data};
    try {
      return (ByteString) literalByteString.newInstance(intArgs);
    } catch (InstantiationException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException("can not new ByteString instance");
    }
  }

  public static ByteString newInstance(byte[] data, int offset, int length) {
    Object[] intArgs = new Object[]{data, offset, length};
    try {
      return (ByteString) boundedByteString.newInstance(intArgs);
    } catch (InstantiationException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException(
          "can not new ByteString instance" + data + ", offset: " + offset + ", length: "
              + length);
    }
  }

  public static ByteBuffer getReadOnlyByteBuffer(ByteString byteString) {
    ByteBuffer byteBuffer = byteString.asReadOnlyByteBuffer();
    try {
      isReadOnlyFieldForByteBuffer.set(byteBuffer, false);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      logger.error("caught an exception", e);
      throw new RuntimeException();
    }
    try {
      byteBuffer.array();
    } catch (Exception e) {
      logger.info("cannot get bytebuffer directly , do a memory copy");
      return ByteBuffer.wrap(byteString.toByteArray());
    }
    return byteBuffer;
  }
}
