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

import java.util.Map;
import py.common.RequestIdBuilder;

public class AlertTemplate {
  String id;
  String name;
  String sourceId;
  Map<String, AlertRule> alertRuleMap;

  public AlertTemplate() {
    this.id = String.valueOf(RequestIdBuilder.get());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  public Map<String, AlertRule> getAlertRuleMap() {
    return alertRuleMap;
  }

  public void setAlertRuleMap(Map<String, AlertRule> alertRuleMap) {
    this.alertRuleMap = alertRuleMap;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AlertTemplate that = (AlertTemplate) o;

    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (sourceId != null ? !sourceId.equals(that.sourceId) : that.sourceId != null) {
      return false;
    }
    return alertRuleMap != null ? alertRuleMap.equals(that.alertRuleMap)
        : that.alertRuleMap == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (sourceId != null ? sourceId.hashCode() : 0);
    result = 31 * result + (alertRuleMap != null ? alertRuleMap.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "AlertTemplate{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", sourceId='"
        + sourceId + '\''
        + ", alertRuleMap=" + alertRuleMap + '}';
  }
}