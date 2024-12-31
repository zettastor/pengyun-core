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

package py.common.struct;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.buffer.ByteBuf;
import io.netty.util.Timeout;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class EchoFutureMessage {
  private final SettableFuture<ByteBuf> response;
  private Timeout receiveTimeout;

  public EchoFutureMessage() {
    this.response = SettableFuture.create();
  }

  public void onResponseReceived(ByteBuf response) {
    this.response.set(response);
  }

  public ListenableFuture<ByteBuf> getResponse() {
    return response;
  }

  public void setTimeout(Timeout rcvtimeout) {
    receiveTimeout = rcvtimeout;
  }

  public void clearTimeout() {
    receiveTimeout.cancel();
  }

  public boolean isArrived() {
    return (response.isDone());
  }

  public byte[] readAvailableByteArray(int waitMills) {
    ByteBuf chbuffer = null;
    try {
      chbuffer = response.get(waitMills, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      return null;
    }

    int len = chbuffer.readableBytes();

    byte[] srcdata = chbuffer.readBytes(len).array();
    return srcdata;
  }

  public ByteBuffer readAvailableByteBuffer(int waitMills) {
    ByteBuf chbuffer = null;
    try {
      chbuffer = response.get(waitMills, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      return null;
    }

    int len = chbuffer.readableBytes();

    return chbuffer.readBytes(len).nioBuffer(0, len);
  }

  public void releaseBuffer() {
    ByteBuf chbuffer = null;
    try {
      chbuffer = response.get();
    } catch (Exception e) {
      return;
    }
    if (chbuffer != null) {
      chbuffer.release();
    }
  }

}
