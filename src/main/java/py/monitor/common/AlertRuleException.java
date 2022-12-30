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

import com.google.common.base.Enums;

public enum AlertRuleException {
  ALERT_LEVEL_ONE_CANNOT_BE_NULL("alert level one cannot be null",
      "告警等级一不能为空"),
  RECOVERY_ALERT_LEVEL_CANNOT_BE_NULL("recovery alert level cannot be null",
      "恢复告警等级不能为空"),
  ALERT_RELATION_OPERATOR_CANNOT_BE_NULL("alert relation operator cannot be null",
      "告警关系操作符不能为空"),
  ALERT_RECOVERY_RELATION_OPERATOR_CANNOT_BE_NULL("alert recovery relation operator "
      + "cannot be null",
      "告警恢复关系操作符不能为空"),

  ALERT_LEVEL_ONE_CANNOT_BE_CLEARED("alert level one cannot be CLEARED",
      "告警等级一不能为已清除"),
  ALERT_LEVEL_TWO_CANNOT_BE_CLEARED("alert level two cannot be CLEARED",
      "告警等级二不能为已清除"),
  ALERT_LEVEL_ONE_CANNOT_LET_ALERT_RECOVERY_LEVEL("alert level one cannot LET alert "
      + "recovery level",
      "告警等级一不能小于等于告警恢复等级"),
  ALERT_LEVEL_ONE_CANNOT_LET_ALERT_LEVEL_TWO("alert level one cannot LET alert level two",
      "告警等级一不能小于等于告警等级二"),
  ALERT_LEVEL_TWO_CANNOT_LET_ALERT_RECOVERY_LEVEL("alert level two cannot LET alert "
      + "recovery level",
      "告警等级二不能小于等于告警恢复等级"),

  ALERT_AND_ALERT_RECOVERY_RELATION_OPERATOR_IS_ILLEGAL(
      "alert and alert recovery relation operator is illegal",
      "告警和告警恢复关系操作符有交集, 不合法"),
  ALERT_RELATION_OPERATOR_CANNOT_BE_EQ("alert relation operator cannot be EQ",
      "告警关系操作符不能设为等于"),
  ALERT_RELATION_OPERATOR_CANNOT_BE_NEQ("alert relation operator cannot be NEQ",
      "告警关系操作符不能设为不等于"),
  ALERT_RECOVERY_RELATION_OPERATOR_CANNOT_BE_NEQ("recovery alert relation operator "
      + "cannot be NEQ",
      "告警恢复关系操作符不能设为不等于"),

  ALERT_RECOVERY_THRESHOLD_CANNOT_GT_ALERT_LEVEL_ONE_THRESHOLD(
      "alert recovery threshold cannot GT alert level one "
          + "threshold", "告警恢复阈值不能大于告警等级一阈值"),
  ALERT_LEVEL_TWO_THRESHOLD_CANNOT_GET_ALERT_LEVEL_ONE_THRESHOLD(
      "alert level two threshold cannot GET alert level one "
          + "threshold", "告警等级二阈值不能大于等于告警等级一阈值"),
  ALERT_RECOVERY_THRESHOLD_CANNOT_GT_ALERT_LEVEL_TWO_THRESHOLD(
      "alert recovery threshold cannot GT alert level two"
          + "threshold", "告警恢复阈值不能大于告警等级二阈值"),
  ALERT_RECOVERY_THRESHOLD_CANNOT_GET_ALERT_LEVEL_ONE_THRESHOLD(
      "alert recovery threshold cannot GET alert level one "
          + "threshold", "告警恢复阈值不能大于等于告警等级一阈值"),
  ALERT_RECOVERY_THRESHOLD_CANNOT_GET_ALERT_LEVEL_TWO_THRESHOLD(
      "alert recovery threshold cannot GET alert level two"
          + "threshold", "告警恢复阈值不能大于等于告警等级二阈值"),

  ALERT_RECOVERY_THRESHOLD_CANNOT_LT_ALERT_LEVEL_ONE_THRESHOLD(
      "alert recovery threshold cannot LT alert level one "
          + "threshold", "告警恢复阈值不能小于告警等级一阈值"),
  ALERT_LEVEL_TWO_THRESHOLD_CANNOT_LET_ALERT_LEVEL_ONE_THRESHOLD(
      "alert level two threshold cannot LET alert level one "
          + "threshold", "告警等级二阈值不能小于等于告警等级一阈值"),
  ALERT_RECOVERY_THRESHOLD_CANNOT_LT_ALERT_LEVEL_TWO_THRESHOLD(
      "alert recovery threshold cannot LT alert level two"
          + "threshold", "告警恢复阈值不能小于告警等级二阈值"),
  ALERT_RECOVERY_THRESHOLD_CANNOT_LET_ALERT_LEVEL_ONE_THRESHOLD(
      "alert recovery threshold cannot LET alert level one "
          + "threshold", "告警恢复阈值不能小于等于告警等级一阈值"),
  ALERT_RECOVERY_THRESHOLD_CANNOT_LET_ALERT_LEVEL_TWO_THRESHOLD(
      "alert recovery threshold cannot LET alert level two"
          + "threshold", "告警恢复阈值不能小于等于告警等级二阈值");

  private String enName;
  private String cnName;

  AlertRuleException(String enName, String cnName) {
    this.enName = enName;
    this.cnName = cnName;
  }

  public static AlertRuleException getAlertRuleException(String name) {
    return Enums.getIfPresent(AlertRuleException.class, name).orNull();
  }

  public String getEnName() {
    return enName;
  }

  public String getCnName() {
    return cnName;
  }
}
