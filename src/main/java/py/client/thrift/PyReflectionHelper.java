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
