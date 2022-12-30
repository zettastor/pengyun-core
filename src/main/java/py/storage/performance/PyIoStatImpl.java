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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PyIoStatImpl implements PyIoStat {
  private static final Logger logger = LoggerFactory.getLogger(PyIoStatImpl.class);

  @Override
  public long getIoAwait(IoStatElement elementPrevious, IoStatElement elementNow) {
    if (elementPrevious == null || elementNow == null) {
      return 0;
    }
    if (elementPrevious.getReadIos() == 0 && elementPrevious.getWriteIos() == 0
        && elementPrevious.getDiscardIos() == 0) {
      return 0;
    }
    long totalIos =
        elementNow.getReadIos() - elementPrevious.getReadIos() + elementNow.getWriteIos()
            - elementPrevious.getWriteIos() + elementNow.getDiscardIos() - elementPrevious
            .getDiscardIos();
    if (totalIos <= 0) {
      return 0;
    }

    long totalTicks =
        elementNow.getReadTicks() - elementPrevious.getReadTicks() + elementNow.getWriteTicks()
            - elementPrevious.getWriteTicks() + elementNow.getDiscardTicks() - elementPrevious
            .getDiscardTicks();
    if (totalTicks <= 0) {
      return 0;
    }
    return totalTicks / totalIos;
  }

  @Override
  public IoStatElement readDiskStat(String fileName, String deviceName) {
    
    IoStatElement element = null;
    BufferedReader reader = null;

    Validate.isTrue(deviceName != null);

    try {
      reader = new BufferedReader(new FileReader(fileName));
    } catch (FileNotFoundException e) {
      logger.error("file reader error.{}. ", fileName, e);
      return null;
    }

    try {
      String line;
      while ((line = reader.readLine()) != null) {
        logger.debug("file:{} device name:{} line:{}", fileName, deviceName, line);

        String[] sinfos = line.trim().split("\\s+");
        String diskName = sinfos[2];

        if (!deviceName.equals(diskName)) {
          continue;
        }

        if (sinfos.length >= 14) {
          element = new IoStatElement();

          element.setReadIos(Long.valueOf(sinfos[3]));
          element.setReadMerges(Long.valueOf(sinfos[4]));
          element.setReadSectors(Long.valueOf(sinfos[5]));
          element.setReadTicks(Long.valueOf(sinfos[6]));

          element.setWriteIos(Long.valueOf(sinfos[7]));
          element.setWriteMerges(Long.valueOf(sinfos[8]));
          element.setWriteSectors(Long.valueOf(sinfos[9]));
          element.setWriteTicks(Long.valueOf(sinfos[10]));

          element.setIosProgress(Long.valueOf(sinfos[11]));
          element.setTotalTicks(Long.valueOf(sinfos[12]));
          element.setRequestTicks(Long.valueOf(sinfos[13]));

          if (sinfos.length == 18) {
            element.setDiscardIos(Long.valueOf(sinfos[14]));
            element.setDiscardMerges(Long.valueOf(sinfos[15]));
            element.setDiscardSectors(Long.valueOf(sinfos[16]));
            element.setDiscardTicks(Long.valueOf(sinfos[17]));
          }

        } else {
          logger.error("iostat unknown format. device name:{} line:{}", deviceName, line);
        }
        break;
      }
    } catch (IOException e) {
      logger.error("readline exception.", e);
    }

    try {
      reader.close();
    } catch (IOException e) {
      logger.error("reader close error.", e);
    }
    return element;
  }

}
