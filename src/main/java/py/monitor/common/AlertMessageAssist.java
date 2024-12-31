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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class AlertMessageAssist {
  private AtomicInteger levelOneAlertFrequency;
  private AtomicInteger levelTwoAlertFrequency;
  private AtomicInteger recoveryAlertFrequency;
  private Set<EventLogInfo> levelOneEventLogInfoSet;
  private Set<EventLogInfo> levelTwoEventLogInfoSet;
  private Set<EventLogInfo> recoveryAlertEventLogInfoSet;

  public AlertMessageAssist() {
    levelOneAlertFrequency = new AtomicInteger();
    levelTwoAlertFrequency = new AtomicInteger();
    recoveryAlertFrequency = new AtomicInteger();
    levelOneEventLogInfoSet = new HashSet<>();
    levelTwoEventLogInfoSet = new HashSet<>();
    recoveryAlertEventLogInfoSet = new HashSet<>();
  }

  public void addToLevelOneEventLogInfoSet(EventLogInfo eventLogInfo) {
    this.levelOneEventLogInfoSet.add(eventLogInfo);
  }

  public void addToLevelTwoEventLogInfoSet(EventLogInfo eventLogInfo) {
    this.levelTwoEventLogInfoSet.add(eventLogInfo);
  }

  public void addToRecoveryAlertEventLogInfoSet(EventLogInfo eventLogInfo) {
    this.recoveryAlertEventLogInfoSet.add(eventLogInfo);
  }

  public void clearLevelOneEventLogInfoSet() {
    this.levelOneEventLogInfoSet.clear();
  }

  public void clearLevelTwoEventLogInfoSet() {
    this.levelTwoEventLogInfoSet.clear();
  }

  public void clearRecoveryAlertEventLogInfoSet() {
    this.recoveryAlertEventLogInfoSet.clear();
  }

  public Set<EventLogInfo> getLevelOneEventLogInfoSet() {
    return new HashSet<>(levelOneEventLogInfoSet);
  }

  public Set<EventLogInfo> getLevelTwoEventLogInfoSet() {
    return new HashSet<>(levelTwoEventLogInfoSet);
  }

  public Set<EventLogInfo> getRecoveryAlertEventLogInfoSet() {
    return new HashSet<>(recoveryAlertEventLogInfoSet);
  }

  public AtomicInteger getLevelOneAlertFrequency() {
    return levelOneAlertFrequency;
  }

  public AtomicInteger getLevelTwoAlertFrequency() {
    return levelTwoAlertFrequency;
  }

  public AtomicInteger getRecoveryAlertFrequency() {
    return recoveryAlertFrequency;
  }

}
