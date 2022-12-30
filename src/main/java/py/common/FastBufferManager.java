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

/**
 * A class to manage buffer's allocation and release.
 */
public interface FastBufferManager {
  /**
   * Allocate buffer with specified size.
   *
   * <p>If available buffer size is less than specified size, this method will throw out {@link
   * NoAvailableBufferException}.
   */
  public FastBuffer allocateBuffer(long size) throws NoAvailableBufferException;

  /**
   * Allocate multiple buffers with specified size.
   */
  public List<FastBuffer> allocateBuffers(long size) throws NoAvailableBufferException;

  /**
   * Release buffer size.
   */
  public void releaseBuffer(FastBuffer retbuf);

  /**
   * Close buffer.
   */
  public void close();

  public long size();

  public long getAlignmentSize();
}