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

package py.io.qos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum IoLimitationType {
  Static(1),
  Dynamic(2);

  private static final Logger logger = LoggerFactory.getLogger(IoLimitationStatus.class);
  private int value;

  private IoLimitationType(int value) {
    this.setValue(value);
  }

  public static IoLimitationType findByName(String name) {
    switch (name) {
      case "Static":
        return Static;
      case "Dynamic":
        return Dynamic;
      default:
        logger.error("can not find IOLimitationType by name:{}", name);
        return null;
    }
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }
}
