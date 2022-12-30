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
