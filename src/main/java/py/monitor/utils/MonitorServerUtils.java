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

package py.monitor.utils;

import java.util.Map;
import py.monitor.common.AlertRule;
import py.monitor.common.AlertTemplate;
import py.monitor.common.CounterName;
import py.monitor.common.MonitorObjectEnum;

public class MonitorServerUtils {
  public static final String MIX_ALERT_RULE_DELIMITER = "##";

  private MonitorServerUtils() {
  }

  public static MonitorObjectEnum getMonitorObjectEnum(AlertRule alertRule,
      AlertTemplate alertTemplate) {
    while (alertRule.getCounterKey().contains(MIX_ALERT_RULE_DELIMITER)) {
      String leftId = alertRule.getLeftId();
      Map<String, AlertRule> alertRuleMap = alertTemplate.getAlertRuleMap();
      for (Map.Entry<String, AlertRule> entry : alertRuleMap.entrySet()) {
        AlertRule value = entry.getValue();
        if (value.getId().equals(leftId)) {
          alertRule = value;
          break;
        }
      }
    }
    return CounterName.valueOf(alertRule.getCounterKey()).getMonitorObjectEnum();
  }

}
