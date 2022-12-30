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

package py.monitor.alarmbak;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.exception.IllegalIndexException;

public enum AlarmLevel implements Comparable<AlarmLevel>, Serializable {
  CRITICAL(1),

  MAJOR(2),

  MINOR(3),

  WARNING(4);

  private static final Logger logger = LoggerFactory.getLogger(AlarmLevel.class);

  private final int value;

  private AlarmLevel(int value) {
    this.value = value;
  }

  public static AlarmLevel get(int portTypeIndex) throws IllegalIndexException {
    for (AlarmLevel portType : values()) {
      if (portTypeIndex == portType.getValue()) {
        return portType;
      }
    }

    logger.error("The index : {} is out of bound");
    throw new IllegalIndexException();
  }

  public int getValue() {
    return value;
  }

}
