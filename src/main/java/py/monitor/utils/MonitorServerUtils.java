

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
