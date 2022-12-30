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

import py.common.RequestIdBuilder;

public class AlertRule {
  private String id;
  private String name;
  private String counterKey;
  private String description;
  private String alertLevelOne;
  private String alertLevelTwo;
  private int alertLevelOneThreshold;
  private int alertLevelTwoThreshold;
  private String alertRecoveryLevel;
  private String relationOperator;
  private int continuousOccurTimes;
  private boolean repeatAlert;
  private String leftId;
  private String rightId;
  private String parentId;
  private String logicOperator;
  private int alertRecoveryThreshold;
  private int alertRecoveryEventContinuousOccurTimes;
  private String alertRecoveryRelationOperator;
  private boolean enable;
  private AlertTemplate alertTemplate;

  public AlertRule() {
    this.id = String.valueOf(RequestIdBuilder.get());
    this.alertLevelOneThreshold = -1;
    this.alertLevelTwoThreshold = -1;
    this.enable = true;
  }

  public AlertRule(String name, String counterKey, String description, String alertLevelOne,
      String alertLevelTwo,
      int alertLevelOneThreshold, int alertLevelTwoThreshold, String relationOperator, int
      continuousOccurTimes, boolean repeatAlert, String leftId, String rightId, String
      parentId, String logicOperator, int alertRecoveryThreshold,
      String alertRecoveryRelationOperator, int alertRecoveryEventContinuousOccurTimes,
      AlertTemplate
          alertTemplate) {
    this.id = String.valueOf(RequestIdBuilder.get());
    this.name = name;
    this.counterKey = counterKey;
    this.description = description;
    this.alertLevelOne = alertLevelOne;
    this.alertLevelTwo = alertLevelTwo;
    this.alertLevelOneThreshold = alertLevelOneThreshold;
    this.alertLevelTwoThreshold = alertLevelTwoThreshold;
    this.relationOperator = relationOperator;
    this.continuousOccurTimes = continuousOccurTimes;
    this.repeatAlert = repeatAlert;
    this.leftId = leftId;
    this.rightId = rightId;
    this.parentId = parentId;
    this.logicOperator = logicOperator;
    this.alertRecoveryThreshold = alertRecoveryThreshold;
    this.alertRecoveryRelationOperator = alertRecoveryRelationOperator;
    this.alertRecoveryEventContinuousOccurTimes = alertRecoveryEventContinuousOccurTimes;
    this.enable = true;
    this.alertTemplate = alertTemplate;
  }

  public boolean isLeaf() {
    return leftId == null && rightId == null;
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

  public String getCounterKey() {
    return counterKey;
  }

  public void setCounterKey(String counterKey) {
    this.counterKey = counterKey;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAlertLevelOne() {
    return alertLevelOne;
  }

  public void setAlertLevelOne(String alertLevelOne) {
    this.alertLevelOne = alertLevelOne;
  }

  public String getAlertLevelTwo() {
    return alertLevelTwo;
  }

  public void setAlertLevelTwo(String alertLevelTwo) {
    this.alertLevelTwo = alertLevelTwo;
  }

  public int getAlertLevelOneThreshold() {
    return alertLevelOneThreshold;
  }

  public void setAlertLevelOneThreshold(int alertLevelOneThreshold) {
    this.alertLevelOneThreshold = alertLevelOneThreshold;
  }

  public int getAlertLevelTwoThreshold() {
    return alertLevelTwoThreshold;
  }

  public void setAlertLevelTwoThreshold(int alertLevelTwoThreshold) {
    this.alertLevelTwoThreshold = alertLevelTwoThreshold;
  }

  public String getRelationOperator() {
    return relationOperator;
  }

  public void setRelationOperator(String relationOperator) {
    this.relationOperator = relationOperator;
  }

  public int getContinuousOccurTimes() {
    return continuousOccurTimes;
  }

  public void setContinuousOccurTimes(int continuousOccurTimes) {
    this.continuousOccurTimes = continuousOccurTimes;
  }

  public boolean isRepeatAlert() {
    return repeatAlert;
  }

  public void setRepeatAlert(boolean repeatAlert) {
    this.repeatAlert = repeatAlert;
  }

  public String getLeftId() {
    return leftId;
  }

  public void setLeftId(String leftId) {
    this.leftId = leftId;
  }

  public String getRightId() {
    return rightId;
  }

  public void setRightId(String rightId) {
    this.rightId = rightId;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getLogicOperator() {
    return logicOperator;
  }

  public void setLogicOperator(String logicOperator) {
    this.logicOperator = logicOperator;
  }

  public int getAlertRecoveryThreshold() {
    return alertRecoveryThreshold;
  }

  public void setAlertRecoveryThreshold(int alertRecoveryThreshold) {
    this.alertRecoveryThreshold = alertRecoveryThreshold;
  }

  public String getAlertRecoveryLevel() {
    return alertRecoveryLevel;
  }

  public void setAlertRecoveryLevel(String alertRecoveryLevel) {
    this.alertRecoveryLevel = alertRecoveryLevel;
  }

  public int getAlertRecoveryEventContinuousOccurTimes() {
    return alertRecoveryEventContinuousOccurTimes;
  }

  public void setAlertRecoveryEventContinuousOccurTimes(
      int alertRecoveryEventContinuousOccurTimes) {
    this.alertRecoveryEventContinuousOccurTimes = alertRecoveryEventContinuousOccurTimes;
  }

  public String getAlertRecoveryRelationOperator() {
    return alertRecoveryRelationOperator;
  }

  public void setAlertRecoveryRelationOperator(String alertRecoveryRelationOperator) {
    this.alertRecoveryRelationOperator = alertRecoveryRelationOperator;
  }

  public boolean isEnable() {
    return enable;
  }

  public void setEnable(boolean enable) {
    this.enable = enable;
  }

  public AlertTemplate getAlertTemplate() {
    return alertTemplate;
  }

  public void setAlertTemplate(AlertTemplate alertTemplate) {
    this.alertTemplate = alertTemplate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AlertRule alertRule = (AlertRule) o;

    if (alertLevelOneThreshold != alertRule.alertLevelOneThreshold) {
      return false;
    }
    if (alertLevelTwoThreshold != alertRule.alertLevelTwoThreshold) {
      return false;
    }
    if (continuousOccurTimes != alertRule.continuousOccurTimes) {
      return false;
    }
    if (repeatAlert != alertRule.repeatAlert) {
      return false;
    }
    if (id != null ? !id.equals(alertRule.id) : alertRule.id != null) {
      return false;
    }
    if (name != null ? !name.equals(alertRule.name) : alertRule.name != null) {
      return false;
    }
    if (counterKey != null ? !counterKey.equals(alertRule.counterKey)
        : alertRule.counterKey != null) {
      return false;
    }
    if (description != null ? !description.equals(alertRule.description)
        : alertRule.description != null) {
      return false;
    }
    if (alertLevelOne != null ? !alertLevelOne.equals(alertRule.alertLevelOne)
        : alertRule.alertLevelOne != null) {
      return false;
    }
    if (alertLevelTwo != null ? !alertLevelTwo.equals(alertRule.alertLevelTwo)
        : alertRule.alertLevelTwo != null) {
      return false;
    }
    if (relationOperator != null ? !relationOperator.equals(alertRule.relationOperator) : alertRule
        .relationOperator != null) {
      return false;
    }
    if (leftId != null ? !leftId.equals(alertRule.leftId) : alertRule.leftId != null) {
      return false;
    }
    if (rightId != null ? !rightId.equals(alertRule.rightId) : alertRule.rightId != null) {
      return false;
    }
    if (parentId != null ? !parentId.equals(alertRule.parentId) : alertRule.parentId != null) {
      return false;
    }
    if (logicOperator != null ? !logicOperator.equals(alertRule.logicOperator)
        : alertRule.logicOperator != null) {
      return false;
    }
    if (alertRecoveryThreshold != alertRule.alertRecoveryThreshold) {
      return false;
    }
    if (alertRecoveryEventContinuousOccurTimes
        != alertRule.alertRecoveryEventContinuousOccurTimes) {
      return false;
    }
    if (alertRecoveryRelationOperator != null ? !alertRecoveryRelationOperator.equals(alertRule
        .alertRecoveryRelationOperator) : alertRule.alertRecoveryRelationOperator != null) {
      return false;
    }
    return alertTemplate != null ? alertTemplate.equals(alertRule.alertTemplate)
        : alertRule.alertTemplate == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (counterKey != null ? counterKey.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (alertLevelOne != null ? alertLevelOne.hashCode() : 0);
    result = 31 * result + (alertLevelTwo != null ? alertLevelTwo.hashCode() : 0);
    result = 31 * result + alertLevelOneThreshold;
    result = 31 * result + alertLevelTwoThreshold;
    result = 31 * result + alertRecoveryThreshold;
    result = 31 * result + alertRecoveryEventContinuousOccurTimes;
    result = 31 * result + (relationOperator != null ? relationOperator.hashCode() : 0);
    result = 31 * result + (alertRecoveryRelationOperator != null ? alertRecoveryRelationOperator
        .hashCode() : 0);
    result = 31 * result + continuousOccurTimes;
    result = 31 * result + (repeatAlert ? 1 : 0);
    result = 31 * result + (leftId != null ? leftId.hashCode() : 0);
    result = 31 * result + (rightId != null ? rightId.hashCode() : 0);
    result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
    result = 31 * result + (logicOperator != null ? logicOperator.hashCode() : 0);
    result = 31 * result + (alertTemplate != null ? alertTemplate.getId().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "AlertRule{" + "id='" + id
        + '\'' + ", name='" + name
        + '\'' + ", counterKey='" + counterKey + '\''
        + ", description='" + description + '\''
        + ", alertLevelOne='" + alertLevelOne + '\'' + ", "
        + "alertLevelTwo='" + alertLevelTwo + '\''
        + ", alertLevelOneThreshold='" + alertLevelOneThreshold + '\''
        + ", alertLevelTwoThreshold='" + alertLevelTwoThreshold + '\''
        + ", relationOperator='" + relationOperator + '\''
        + ", continuousOccurTimes=" + continuousOccurTimes
        + ", repeatAlert=" + repeatAlert
        + ", leftId='" + leftId + '\''
        + ", rightId='" + rightId + '\''
        + ", parentId='" + parentId + '\''
        + ", logicOperator='" + logicOperator + '\''
        + '\'' + ", alertRecoveryRelationOperator='" + alertRecoveryRelationOperator + '\''
        + ", alertRecoveryThreshold='" + alertRecoveryThreshold + '\'' + ", "
        + "alertRecoveryEventContinuousOccurTimes='" + alertRecoveryEventContinuousOccurTimes + '\''
        + ", " + ", enable='" + enable + '\''
        + "alertTemplateName=" + (alertTemplate == null ? null : alertTemplate.getName())
        + '}';
  }
}