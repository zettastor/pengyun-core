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

package py.storage.impl;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import py.exception.StorageException;
import py.storage.Storage;

public abstract class AsyncStorage extends Storage {
  public AsyncStorage(String identifier) {
    super(identifier);
  }

  public abstract <A> void read(ByteBuffer buffer, long pos, A attachment,
      CompletionHandler<Integer, ? super A> handler) throws StorageException;

  @Override
  public void read(long pos, byte[] dstBuf, int off, int len) throws StorageException {
    read(pos, ByteBuffer.wrap(dstBuf, off, len));
  }

  @Override
  public void read(long pos, ByteBuffer buffer) throws StorageException {
    CompletableFuture<Throwable> future = new CompletableFuture<>();
    read(buffer, pos, null, new CompletionHandler<Integer, Void>() {
      @Override
      public void completed(Integer result, Void attachment) {
        if (result <= 0) {
          future.complete(new StorageException("result <= zero. throw exception"));
        } else {
          future.complete(null);
        }
      }

      @Override
      public void failed(Throwable exc, Void attachment) {
        future.complete(exc);
      }
    });

    try {
      Throwable exception = future.get();
      if (exception != null) {
        throw new StorageException(exception);
      }
    } catch (Exception e) {
      throw new StorageException(e);
    }
  }

  public abstract <A> void write(ByteBuffer buffer, long pos, A attachment,
      CompletionHandler<Integer, ? super A> handler) throws StorageException;

  @Override
  public void write(long pos, byte[] buf, int off, int len) throws StorageException {
    write(pos, ByteBuffer.wrap(buf, off, len));
  }

  @Override
  public void write(long pos, ByteBuffer buffer) throws StorageException {
    CompletableFuture<Throwable> future = new CompletableFuture<>();
    write(buffer, pos, null, new CompletionHandler<Integer, Void>() {
      @Override
      public void completed(Integer result, Void attachment) {
        future.complete(null);
      }

      @Override
      public void failed(Throwable exc, Void attachment) {
        future.complete(exc);
      }
    });

    try {
      Throwable exception = future.get();
      if (exception != null) {
        throw new StorageException(exception);
      }
    } catch (Exception e) {
      throw new StorageException(e);
    }
  }

}
