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

import java.util.Objects;

public class AlertRuleAndObjectKey {
  private String alertTemplateId;
  private String counterKey;
  private String sourceKey;

  public AlertRuleAndObjectKey() {
  }

  public AlertRuleAndObjectKey(String alertTemplateId, String counterKey, String sourceKey) {
    this.alertTemplateId = alertTemplateId;
    this.counterKey = counterKey;
    this.sourceKey = sourceKey;
  }

  public String getAlertTemplateId() {
    return alertTemplateId;
  }

  public void setAlertTemplateId(String alertTemplateId) {
    this.alertTemplateId = alertTemplateId;
  }

  public String getCounterKey() {
    return counterKey;
  }

  public void setCounterKey(String counterKey) {
    this.counterKey = counterKey;
  }

  public String getSourceKey() {
    return sourceKey;
  }

  public void setSourceKey(String sourceKey) {
    this.sourceKey = sourceKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertRuleAndObjectKey that = (AlertRuleAndObjectKey) o;
    return Objects.equals(alertTemplateId, that.alertTemplateId) && Objects
        .equals(counterKey, that.counterKey)
        && Objects.equals(sourceKey, that.sourceKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(alertTemplateId, counterKey, sourceKey);
  }

  @Override
  public String toString() {
    return "AlertRuleAndObjectKey{" + "alertTemplateId='" + alertTemplateId + '\''
        + ", counterKey='" + counterKey + '\''
        + ", sourceKey='" + sourceKey + '\''
        + '}';
  }
}
