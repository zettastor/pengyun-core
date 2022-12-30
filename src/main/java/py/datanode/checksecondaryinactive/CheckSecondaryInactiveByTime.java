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

package py.datanode.checksecondaryinactive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CheckSecondaryInactiveByTime implements CheckSecondaryInactive {
  private static final Logger logger = LoggerFactory.getLogger(CheckSecondaryInactiveByTime.class);

  public static long missPagesThreshold;

  public static long missLogsThreshold;

  public boolean ignoreMissPagesAndLogs = false;

  public CheckSecondaryInactiveByTime(boolean ignoreMissPagesAndLogs) {
    this.ignoreMissPagesAndLogs = ignoreMissPagesAndLogs;
  }

  public static void initMissCount(long missPagesThresholdCount, long missLogsThresholdCount) {
    missPagesThreshold = missPagesThresholdCount;
    missLogsThreshold = missLogsThresholdCount;
  }

  @Override
  public boolean missTooManyLogs(long missLogs) {
    logger
        .debug("ignore mis page and logs{},missPage is {},threshold is {}", ignoreMissPagesAndLogs,
            missLogs,
            missLogsThreshold);
    if (ignoreMissPagesAndLogs) {
      return false;
    }

    return missLogs >= missLogsThreshold ? true : false;
  }

  @Override
  public boolean missTooManyPages(long missPages) {
    logger
        .debug("ignore mis page and logs{},missPage is {},threshold is {}", ignoreMissPagesAndLogs,
            missPages,
            missPagesThreshold);
    if (ignoreMissPagesAndLogs) {
      return false;
    }
    return missPages >= missPagesThreshold ? true : false;
  }

  public boolean isIgnoreMissPagesAndLogs() {
    return ignoreMissPagesAndLogs;
  }

  public void setIgnoreMissPagesAndLogs(boolean ignoreMissPagesAndLogs) {
    this.ignoreMissPagesAndLogs = ignoreMissPagesAndLogs;
  }

  public long getMissPagesThreshold() {
    return missPagesThreshold;
  }

  public long getMissLogsThreshold() {
    return missLogsThreshold;
  }

}
