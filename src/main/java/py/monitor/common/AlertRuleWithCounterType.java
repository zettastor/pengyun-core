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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

public class AlertRuleWithCounterType {
  private String id;
  private String name;
  private String counterKey;
  private String description;
  private String alertLevelOne;
  private String alertLevelTwo;
  private int alertLevelOneThreshold;
  private int alertLevelTwoThreshold;
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
  @JsonIgnore
  private AlertTemplate alertTemplate;

  private String counterType;
  private AlertRule leftAlertRule;
  private AlertRule rightAlertRule;

  public AlertRuleWithCounterType(AlertRule alertRule) {
    this.id = alertRule.getId();
    this.name = alertRule.getName();
    this.counterKey = alertRule.getCounterKey();
    this.description = alertRule.getDescription();
    this.alertLevelOne = alertRule.getAlertLevelOne();
    this.alertLevelTwo = alertRule.getAlertLevelTwo();
    this.alertLevelOneThreshold = alertRule.getAlertLevelOneThreshold();
    this.alertLevelTwoThreshold = alertRule.getAlertLevelTwoThreshold();
    this.relationOperator = alertRule.getRelationOperator();
    this.continuousOccurTimes = alertRule.getContinuousOccurTimes();
    this.repeatAlert = alertRule.isRepeatAlert();
    this.leftId = alertRule.getLeftId();
    this.rightId = alertRule.getRightId();
    this.parentId = alertRule.getParentId();
    this.logicOperator = alertRule.getLogicOperator();
    this.alertRecoveryThreshold = alertRule.getAlertRecoveryThreshold();
    this.alertRecoveryRelationOperator = alertRule.getAlertRecoveryRelationOperator();
    this.alertRecoveryEventContinuousOccurTimes = alertRule
        .getAlertRecoveryEventContinuousOccurTimes();
    this.enable = alertRule.isEnable();

    try {
      CounterName counterName = CounterName.valueOf(alertRule.getCounterKey());
      if (counterName.getCounterType() == CounterName.CounterType.Threshold) {
        this.counterType = "SIMPLE";
      } else {
        Validate.isTrue(counterName.getCounterType() == CounterName.CounterType.Status);
        this.counterType = "BASE";
      }
    } catch (Exception e) {
      this.counterType = "MIX";
    }

    Map<String, AlertRule> alertRuleMap = alertRule.getAlertTemplate().getAlertRuleMap();
    for (Map.Entry<String, AlertRule> entry : alertRuleMap.entrySet()) {
      AlertRule currentAlertRule = entry.getValue();
      if (Objects.equals(currentAlertRule.getId(), leftId)) {
        leftAlertRule = currentAlertRule;
        leftAlertRule.setAlertTemplate(null);
      }
      if (Objects.equals(currentAlertRule.getId(), rightId)) {
        rightAlertRule = currentAlertRule;
        rightAlertRule.setAlertTemplate(null);
      }
    }
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

  public AlertTemplate getAlertTemplate() {
    return alertTemplate;
  }

  public void setAlertTemplate(AlertTemplate alertTemplate) {
    this.alertTemplate = alertTemplate;
  }

  public String getCounterType() {
    return counterType;
  }

  public void setCounterType(String counterType) {
    this.counterType = counterType;
  }

  public AlertRule getLeftAlertRule() {
    return leftAlertRule;
  }

  public void setLeftAlertRule(AlertRule leftAlertRule) {
    this.leftAlertRule = leftAlertRule;
  }

  public AlertRule getRightAlertRule() {
    return rightAlertRule;
  }

  public void setRightAlertRule(AlertRule rightAlertRule) {
    this.rightAlertRule = rightAlertRule;
  }

  public int getAlertRecoveryThreshold() {
    return alertRecoveryThreshold;
  }

  public void setAlertRecoveryThreshold(int alertRecoveryThreshold) {
    this.alertRecoveryThreshold = alertRecoveryThreshold;
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

  @Override
  public String toString() {
    return "AlertRuleWithCounterType{"
        + "id='" + id + '\''
        + ", name='" + name + '\''
        + ", counterKey='" + counterKey + '\''
        + ", description='" + description + '\''
        + ", alertLevelOne='" + alertLevelOne + '\''
        + ", alertLevelTwo='" + alertLevelTwo + '\''
        + ", alertLevelOneThreshold=" + alertLevelOneThreshold
        + ", alertLevelTwoThreshold=" + alertLevelTwoThreshold
        + ", relationOperator='" + relationOperator + '\''
        + ", continuousOccurTimes=" + continuousOccurTimes
        + ", repeatAlert=" + repeatAlert
        + ", leftId='" + leftId + '\''
        + ", rightId='" + rightId + '\''
        + ", parentId='" + parentId + '\''
        + ", logicOperator='" + logicOperator + '\''
        + ", alertTemplate=" + alertTemplate
        + ", counterType='" + counterType + '\''
        + ", leftAlertRule=" + leftAlertRule
        + ", rightAlertRule=" + rightAlertRule
        + ", enable=" + enable
        + '}';
  }
}
