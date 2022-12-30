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
