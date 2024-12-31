
package py.monitor.common;

import java.util.Map;

public class StartedAlertTemplate {
  Map<String, AlertTemplate> alertTemplateMap;

  public Map<String, AlertTemplate> getAlertTemplateMap() {
    return alertTemplateMap;
  }

  public void setAlertTemplateMap(Map<String, AlertTemplate> alertTemplateMap) {
    this.alertTemplateMap = alertTemplateMap;
  }
}
