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
