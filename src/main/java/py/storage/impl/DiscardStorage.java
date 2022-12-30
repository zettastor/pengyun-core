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
