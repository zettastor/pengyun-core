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

package py.netty.client;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.netty.core.MethodCallback;
import py.netty.exception.TimeoutException;

public class MessageTimerTask<T> implements TimerTask {
  private static final Logger logger = LoggerFactory.getLogger(MessageTimerTask.class);
  public final long requestId;
  private final int ioTimeout;
  private final MethodCallback<T> callback;
  private TimerTaskRemover timerTaskRemover;

  public MessageTimerTask(Long requestId, MethodCallback<T> callback,
      TimerTaskRemover timerTaskRemover, int ioTimeout) {
    this.requestId = requestId;
    this.callback = callback;
    this.timerTaskRemover = timerTaskRemover;
    this.ioTimeout = ioTimeout;
  }

  public MethodCallback<T> getCallback() {
    return callback;
  }

  @Override
  public void run(Timeout timeout) throws Exception {
    MethodCallback<T> callback = timerTaskRemover.removeTimer(requestId);
    if (callback != null) {
      logger.warn("callback is not null. requestId:{}", requestId);
      callback.fail(new TimeoutException("cost time:" + ioTimeout + ", requestId:" + requestId));
    } else {
      logger.warn("callback is null. requestId:{}", requestId);
    }
  }
}
