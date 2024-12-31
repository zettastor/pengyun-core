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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import py.common.NamedThreadFactory;
import py.exception.StorageException;

public class DiscardStorage extends AsyncStorage {
  private static final AtomicInteger GLOBAL_INDEX = new AtomicInteger(0);

  private final ExecutorService executorService;

  private final long size;

  public DiscardStorage(String identifier, long size) {
    super(identifier);
    executorService = Executors
        .newSingleThreadExecutor(
            new NamedThreadFactory("discard-storage-" + GLOBAL_INDEX.incrementAndGet()));
    this.size = size;
  }

  @Override
  public <A> void read(ByteBuffer buffer, long offset, A attachment,
      CompletionHandler<Integer, ? super A> handler)
      throws StorageException {
    executorService.execute(() -> {
      int length = buffer.remaining();
      while (buffer.hasRemaining()) {
        buffer.put((byte) 0);
      }
      handler.completed(length, attachment);
    });
  }

  @Override
  public <A> void write(ByteBuffer buffer, long offset, A attachment,
      CompletionHandler<Integer, ? super A> handler)
      throws StorageException {
    executorService.execute(() -> {
      int length = buffer.remaining();
      while (buffer.hasRemaining()) {
        buffer.get();
      }
      handler.completed(length, attachment);
    });
  }

  @Override
  public long size() {
    return size;
  }

}
