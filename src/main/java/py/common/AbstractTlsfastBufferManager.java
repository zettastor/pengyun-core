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

package py.common;

import java.util.List;
import py.exception.NoAvailableBufferException;

public abstract class AbstractTlsfastBufferManager implements FastBufferManager {
  private TlsfFastBufferManager fastBufferManager;

  public AbstractTlsfastBufferManager(TlsfFastBufferManager fastBufferManager) {
    this.fastBufferManager = fastBufferManager;
  }

  @Override
  public void releaseBuffer(FastBuffer retbuf) {
    List<FastBuffer> fastBuffers = ((AbstractFastBuffer) retbuf).getFastBuffers();
    for (FastBuffer fastBuffer : fastBuffers) {
      fastBufferManager.releaseBuffer(fastBuffer);
    }
  }

  @Override
  public void close() {
    fastBufferManager.close();
  }

  @Override
  public long size() {
    return fastBufferManager.size();
  }

  public long getAlignmentSize() {
    return fastBufferManager.getAlignmentSize();
  }

  public List<FastBuffer> allocateBuffers(long size) throws NoAvailableBufferException {
    return fastBufferManager.allocateBuffers(size);
  }

}
