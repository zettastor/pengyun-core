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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertMessageKey {
  private static final Logger logger = LoggerFactory.getLogger(AlertMessageKey.class);

  private String sourceKey;
  private String counterKey;
  private AlertLevel alertLevel;

  public AlertMessageKey() {
  }

  public AlertMessageKey(String sourceKey, String counterKey, AlertLevel alertLevel) {
    this.sourceKey = sourceKey;
    this.counterKey = counterKey;
    this.alertLevel = alertLevel;
  }

  public String getSourceKey() {
    return sourceKey;
  }

  public void setSourceKey(String sourceKey) {
    this.sourceKey = sourceKey;
  }

  public String getCounterKey() {
    return counterKey;
  }

  public void setCounterKey(String counterKey) {
    this.counterKey = counterKey;
  }

  public AlertLevel getAlertLevel() {
    return alertLevel;
  }

  public void setAlertLevel(AlertLevel alertLevel) {
    this.alertLevel = alertLevel;
  }

  public AlertMessageKey getFuzzyAlertMessageKey() {
    String ip = sourceKey;

    boolean isNetSubHealth = AlertMessage.isNetSubHealthAlert(counterKey);
    if (isNetSubHealth) {
      ip = ip.split(":")[0];
    }

    return new AlertMessageKey(ip, counterKey, alertLevel);
  }

  public boolean isFuzzySourceKey() {
    if (AlertMessage.isNetSubHealthAlert(counterKey)) {
      String[] buf = sourceKey.split(":");
      if (buf.length <= 1) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AlertMessageKey that = (AlertMessageKey) o;

    if (sourceKey != null ? !sourceKey.equals(that.sourceKey) : that.sourceKey != null) {
      return false;
    }
    if (counterKey != null ? !counterKey.equals(that.counterKey) : that.counterKey != null) {
      return false;
    }
    return alertLevel == that.alertLevel;
  }

  @Override
  public int hashCode() {
    int result = sourceKey != null ? sourceKey.hashCode() : 0;
    result = 31 * result + (counterKey != null ? counterKey.hashCode() : 0);
    result = 31 * result + (alertLevel != null ? alertLevel.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "AlertMessageKey{"
        + "sourceKey='" + sourceKey + '\''
        + ", counterKey='" + counterKey
        + '\'' + ", "
        + "alertLevel=" + alertLevel
        + '}';
  }
}
