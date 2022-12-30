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
