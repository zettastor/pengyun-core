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
