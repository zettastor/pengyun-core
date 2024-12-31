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

package py.storage.performance;

import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlowDiskCheckerImpl implements SlowDiskChecker {
  private static final Logger logger = LoggerFactory.getLogger(SlowDiskChecker.class);
  private AtomicLong ioRequest = new AtomicLong(0);
  private Boolean isSlowDisk = false;
  private int overTimes = 0;
  private IoStatElement elementSnapshot;
  private PyIoStat pyioStat;

  public SlowDiskCheckerImpl(PyIoStat pyioStat) {
    this.pyioStat = pyioStat;
  }

  @Override
  public void incomingIo() {
    if (!isSlowDisk) {
      ioRequest.incrementAndGet();
    }
  }

  @Override
  public boolean isSlowDisk() {
    return this.isSlowDisk;
  }

  @Override
  public boolean checking(String diskStatFile, String deviceName, int awaitThreshold) {
    if (isSlowDisk) {
      return true;
    }

    if (ioRequest.get() == 0) {
      return false;
    }

    ioRequest.set(0);

    IoStatElement elementNow = pyioStat.readDiskStat(diskStatFile, deviceName);
    if (elementNow == null) {
      logger.warn("cant' get iostat in file:{} dev:{}", diskStatFile, deviceName);
      return false;
    }

    long await = pyioStat.getIoAwait(this.elementSnapshot, elementNow);

    if (await > awaitThreshold) {
      overTimes++;
      logger.warn("disk await:{} reached threshold:{}. device name:{}", await, awaitThreshold,
          deviceName);
    } else {
      overTimes = 0;
    }

    elementSnapshot = elementNow;
    if (overTimes >= 3) {
      isSlowDisk = true;
      logger.warn("find the slow disk:{}.", deviceName);
      return true;
    } else {
      return false;
    }
  }
}
