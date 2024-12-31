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
