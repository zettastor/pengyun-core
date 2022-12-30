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
import java.lang.reflect.Method;
import org.apache.thrift.async.TAsyncMethodCall;
import org.apache.thrift.protocol.TProtocol;

public class PyReflectionHelper {
  private final Method writeArgs;
  private final Field stateField;
  private final Field callbackField;
  private final Field protocolFactoryField;
  private final Field frameBufferField;
  private final Field timeoutField;
  private final Field seqidField;
  private final Field currentMethod;

  public PyReflectionHelper() throws Exception {
    try {
      writeArgs = TAsyncMethodCall.class.getDeclaredMethod("write_args", TProtocol.class);
      writeArgs.setAccessible(true);

      stateField = TAsyncMethodCall.class.getDeclaredField("state");
      stateField.setAccessible(true);

      callbackField = TAsyncMethodCall.class.getDeclaredField("callback");
      callbackField.setAccessible(true);

      protocolFactoryField = TAsyncMethodCall.class.getDeclaredField("protocolFactory");
      protocolFactoryField.setAccessible(true);

      frameBufferField = TAsyncMethodCall.class.getDeclaredField("frameBuffer");
      frameBufferField.setAccessible(true);

      timeoutField = org.apache.thrift.async.TAsyncClient.class.getDeclaredField("___timeout");
      timeoutField.setAccessible(true);

      seqidField = org.apache.thrift.TServiceClient.class.getDeclaredField("seqid_");
      seqidField.setAccessible(true);

      currentMethod = org.apache.thrift.async.TAsyncClient.class
          .getDeclaredField("___currentMethod");
      currentMethod.setAccessible(true);

    } catch (NoSuchMethodException | SecurityException e) {
      throw new Exception("can not create async client manager");
    }
  }

  public Method getWriteArgs() {
    return writeArgs;
  }

  public Field getStateField() {
    return stateField;
  }

  public Field getCallbackField() {
    return callbackField;
  }

  public Field getProtocolFactoryField() {
    return protocolFactoryField;
  }

  public Field getFrameBufferField() {
    return frameBufferField;
  }

  public Field getTimeoutField() {
    return timeoutField;
  }

  public Field getSeqidField() {
    return seqidField;
  }

  public Field getCurrentMethod() {
    return currentMethod;
  }
}
