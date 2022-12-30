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

package py.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.exception.IllegalIndexException;

public enum PortType {
  CONTROL(0),

  HEARTBEAT(1),

  IO(2),

  MONITOR(3),

  RECONCILER(4);

  private static final Logger logger = LoggerFactory.getLogger(PortType.class);

  private final int value;

  private PortType(int value) {
    this.value = value;
  }

  public static PortType get(int portTypeIndex) throws IllegalIndexException {
    for (PortType portType : values()) {
      if (portTypeIndex == portType.getValue()) {
        return portType;
      }
    }

    logger.error("The port type index : {} is out of bound");
    throw new IllegalIndexException();
  }

  public int getValue() {
    return value;
  }
}
