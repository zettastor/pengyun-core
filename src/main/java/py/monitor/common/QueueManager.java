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

package py.monitor.common;

import java.util.concurrent.LinkedBlockingQueue;

public class QueueManager {
  private LinkedBlockingQueue<PerformanceMessageHistory> eventLogQueue =
      new LinkedBlockingQueue<>();
  private LinkedBlockingQueue<PerformanceMessage> performanceQueue = new LinkedBlockingQueue<>();
  private LinkedBlockingQueue<AlertMessage> alertQueueFilterByRule = new LinkedBlockingQueue<>(
      10240);
  private LinkedBlockingQueue<AlertMessage> alertQueueFilterByTime = new LinkedBlockingQueue<>(
      10240);
  private LinkedBlockingQueue<AlertMessage> alertRecoveryQueue = new LinkedBlockingQueue<>(10240);
  private LinkedBlockingQueue<AlertMessage> alertRecoveryQueueByRule = new LinkedBlockingQueue<>(
      10240);
  private LinkedBlockingQueue<AlertMessage> netSubHealthQueue = new LinkedBlockingQueue<>(10240);

  public LinkedBlockingQueue<PerformanceMessageHistory> getEventLogQueue() {
    return eventLogQueue;
  }

  public LinkedBlockingQueue<AlertMessage> getAlertQueueFilterByRule() {
    return alertQueueFilterByRule;
  }

  public LinkedBlockingQueue<AlertMessage> getAlertQueueFilterByTime() {
    return alertQueueFilterByTime;
  }

  public LinkedBlockingQueue<PerformanceMessage> getPerformanceQueue() {
    return performanceQueue;
  }

  public LinkedBlockingQueue<AlertMessage> getAlertRecoveryQueue() {
    return alertRecoveryQueue;
  }

  public LinkedBlockingQueue<AlertMessage> getAlertRecoveryQueueByRule() {
    return alertRecoveryQueueByRule;
  }

  public LinkedBlockingQueue<AlertMessage> getNetSubHealthQueue() {
    return netSubHealthQueue;
  }
}
