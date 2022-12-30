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
