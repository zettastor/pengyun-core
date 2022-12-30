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

package py.netty.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.netty.core.nio.SingleThreadEventLoop;

public class PyTimeToFlushImpl implements PyTimeToFlush {
  private static final Logger logger = LoggerFactory.getLogger(PyTimeToFlushImpl.class);

  private SingleThreadEventLoop singleThreadEventLoop;
  private int requestCount;

  public PyTimeToFlushImpl(SingleThreadEventLoop singleThreadEventLoop) {
    this.singleThreadEventLoop = singleThreadEventLoop;
  }

  @Override
  public void call() {
    logger.debug("before flush, {} message have been written", this.requestCount);
    this.requestCount = 0;
    this.singleThreadEventLoop.flushAllChannels();
  }

  @Override
  public void incRequestCount() {
    this.requestCount++;
  }
}
