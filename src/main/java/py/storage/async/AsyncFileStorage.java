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

package py.storage.async;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.exception.StorageException;
import py.storage.Storage;
import py.storage.impl.AsyncStorage;

public class AsyncFileStorage extends AsyncStorage {
  private static final Logger logger = LoggerFactory.getLogger(AsyncFileStorage.class);
  private final Path path;
  private final int sectorSize;
  private boolean isOpen;
  private boolean isEnableDiskCheck = false;
  private AsyncFileAccessor accessor;
  private long size;

  public AsyncFileStorage(Path path, int ioDepth, int sectorSize) {
    super(path.toString());
    this.path = path;
    this.ioDepth = ioDepth;
    this.sectorSize = sectorSize;
  }

  @Override
  public synchronized void open() throws StorageException {
    if (isOpen) {
      logger.warn("no need to open again. {}", path);
      return;
    }

    accessor = AsyncFileAccessor.open(path, ioDepth);
    this.size = findUsableStorageSize(sectorSize);
    isOpen = true;
  }

  @Override
  public synchronized void close() throws StorageException {
    if (!isOpen) {
      logger.warn("no need to close a closed storage. {}", path);
      return;
    }

    accessor.close();
    isOpen = false;
  }

  @Override
  public boolean isClosed() {
    return !isOpen;
  }

  public void enableSlowDisk(int seqential, int random, double quartile, int policy,
      Callback callback) {
    if (!isEnableDiskCheck) {
      accessor.enableSlowDisk(seqential, random, quartile, policy, callback);
      isEnableDiskCheck = true;
      logger.warn("storage:{} enable slow disk check.", path);
    } else {
      logger.warn("storage:{} slow disk check already enabled.", path);
    }
  }

  @Override
  public <A> void write(ByteBuffer buffer, long pos, A attachment,
      CompletionHandler<Integer, ? super A> handler) {
    IoUtil.write(buffer, pos, accessor, attachment, handler);
  }

  @Override
  public <A> void read(ByteBuffer buffer, long pos, A attachment,
      CompletionHandler<Integer, ? super A> handler) {
    IoUtil.read(buffer, pos, accessor, attachment, handler);
  }

  @Override
  public long size() {
    return size;
  }

}
