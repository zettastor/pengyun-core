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

import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckSecondaryInactiveByAbsoluteTime extends CheckSecondaryInactiveByTime {
  private static final Logger logger = LoggerFactory
      .getLogger(CheckSecondaryInactiveByAbsoluteTime.class);
  private long startTime;
  private long endTime;

  public CheckSecondaryInactiveByAbsoluteTime(boolean ignoreMissPagesAndLogs, long startTime,
      long endTime) {
    super(ignoreMissPagesAndLogs);
    this.startTime = startTime;
    this.endTime = endTime;
  }

  @Override
  public boolean waitTimeout(long waitTime) {
    Calendar calendar = Calendar.getInstance();
    long currentTime =
        calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60 + calendar
            .get(Calendar.SECOND);
    if (endTime < startTime) {
      if (currentTime <= endTime || currentTime >= startTime) {
        return true;
      }
    } else {
      if (currentTime <= endTime && currentTime >= startTime) {
        return true;
      }
    }
    return false;
  }

  @Override
  public CheckSecondaryInactiveThresholdMode getCheckMode() {
    return CheckSecondaryInactiveThresholdMode.AbsoluteTime;
  }

  public void updateInfo(boolean ignoreMissPagesAndLogs, long startTime, long endTime) {
    super.setIgnoreMissPagesAndLogs(ignoreMissPagesAndLogs);
    this.startTime = startTime;
    this.endTime = endTime;
  }
}
