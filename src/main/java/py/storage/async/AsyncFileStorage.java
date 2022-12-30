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
