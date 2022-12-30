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

public enum MigrationRuleStatus {
  FREE(1),
  APPLING(2),
  APPLIED(3),
  CANCELING(4),

  DELETING(5),
  AVAILABLE(6);

  private static final Logger logger = LoggerFactory.getLogger(MigrationRuleStatus.class);
  private int value;

  private MigrationRuleStatus(int value) {
    this.setValue(value);
  }

  public static MigrationRuleStatus findByName(String name) {
    switch (name) {
      case "FREE":
        return FREE;
      case "APPLING":
        return APPLING;
      case "APPLIED":
        return APPLIED;
      case "CANCELING":
        return CANCELING;
      case "DELETING":
        return DELETING;
      case "AVAILABLE":
        return AVAILABLE;
      default:
        logger.error("can not find MigrationRuleStatus by name:{}", name);
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
