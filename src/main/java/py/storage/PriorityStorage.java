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

package py.storage;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import py.exception.StorageException;
import py.storage.impl.AsyncStorage;

public abstract class PriorityStorage extends AsyncStorage {
  public PriorityStorage(String identifier) {
    super(identifier);
  }

  @Override
  public <A> void read(ByteBuffer buffer, long pos, A attachment,
      CompletionHandler<Integer, ? super A> handler)
      throws StorageException {
    read(buffer, pos, attachment, handler, Priority.HIGH);
  }

  public abstract <A> void read(ByteBuffer buffer, long pos, A attachment,
      CompletionHandler<Integer, ? super A> handler, Priority priority) throws StorageException;

  @Override
  public <A> void write(ByteBuffer buffer, long pos, A attachment,
      CompletionHandler<Integer, ? super A> handler)
      throws StorageException {
    write(buffer, pos, attachment, handler, Priority.HIGH);
  }

  public abstract <A> void write(ByteBuffer buffer, long pos, A attachment,
      CompletionHandler<Integer, ? super A> handler, Priority priority) throws StorageException;

  public enum Priority {
    HIGH(0), MIDDLE(1), LOW(2);

    int val;

    Priority(int val) {
      this.val = val;
    }

    public int getVal() {
      return val;
    }

  }

}
