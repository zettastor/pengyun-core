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
import py.exception.StorageException;

public abstract class NonBlockingStorage extends AsyncStorage {
  public NonBlockingStorage(String identifier) {
    super(identifier);
  }

  public abstract int availablePermitsForWrite();

  public abstract int availablePermitsForRead();

  public abstract boolean preWrite(int permits);

  public abstract boolean preWrite(int permits, Boolean block);

  public abstract void discardWrite(int permits);

  public abstract boolean preRead(int permits);

  public abstract void discardRead(int permits);

  public abstract <A> void nonBlockingWrite(ByteBuffer buffer, long offset, A attachment,
      CompletionHandler<Integer, A> handler) throws StorageException;

  public abstract <A> void nonBlockingRead(ByteBuffer buffer, long offset, A attachment,
      CompletionHandler<Integer, A> handler) throws StorageException;
}
