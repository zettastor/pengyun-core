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
