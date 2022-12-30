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

package py.monitor.common;

public enum AlertLevel {
  
  CRITICAL("严重告警", 0) {
    @Override
    public String getSymbol() {
      return "!!!";
    }
  },
  MAJOR("重要告警", 1) {
    @Override
    public String getSymbol() {
      return "!";
    }
  },
  MINOR("次要告警", 2),
  WARNING("警告", 3),
  CLEARED("已清除", 4),
  INDETERMINATE("待定", 5);

  private String cnName;
  private int level;

  AlertLevel(String cnName, int level) {
    this.cnName = cnName;
    this.level = level;
  }

  public static AlertLevel getLevleByCnName(String cnName) {
    for (AlertLevel alertLevel : AlertLevel.values()) {
      if (alertLevel.getCnName().equals(cnName)) {
        return alertLevel;
      }
    }
    return null;
  }

  public int getLevel() {
    return level;
  }

  public String getCnName() {
    return cnName;
  }

  public String getSymbol() {
    return "";
  }
}
