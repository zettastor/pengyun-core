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
