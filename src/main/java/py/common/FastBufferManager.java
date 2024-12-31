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