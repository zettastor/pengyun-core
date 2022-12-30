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
