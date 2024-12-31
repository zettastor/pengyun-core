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
