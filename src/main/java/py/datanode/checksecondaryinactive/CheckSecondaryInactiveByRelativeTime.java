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

public class CheckSecondaryInactiveByRelativeTime extends CheckSecondaryInactiveByTime {
  private static final Logger logger = LoggerFactory
      .getLogger(CheckSecondaryInactiveByRelativeTime.class);
  private long waitTimerThreshold;

  public CheckSecondaryInactiveByRelativeTime(boolean ignoreMissPagesAndLogs,
      long waitTimerThreshold) {
    super(ignoreMissPagesAndLogs);
    this.waitTimerThreshold = waitTimerThreshold;
  }

  @Override
  public boolean waitTimeout(long waitTime) {
    if (waitTime >= waitTimerThreshold) {
      return true;
    }
    return false;
  }

  @Override
  public CheckSecondaryInactiveThresholdMode getCheckMode() {
    return CheckSecondaryInactiveThresholdMode.RelativeTime;
  }

  public void updateInfo(boolean ignoreMissPagesAndLogs, long waitTimerThreshold) {
    this.waitTimerThreshold = waitTimerThreshold;
    super.setIgnoreMissPagesAndLogs(ignoreMissPagesAndLogs);
  }
}
