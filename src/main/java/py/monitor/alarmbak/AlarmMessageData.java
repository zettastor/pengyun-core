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

package py.monitor.alarmbak;

import java.io.Serializable;

public class AlarmMessageData implements Serializable {
  private static final long serialVersionUID = 1L;
  String alarmObject;
  String alarmName;
  AlarmLevel alarmLevel;
  String alarmDescription;
  private AlarmOper oper;

  public String getAlarmName() {
    return alarmName;
  }

  public void setAlarmName(String alarmName) {
    this.alarmName = alarmName;
  }

  public AlarmLevel getAlarmLevel() {
    return alarmLevel;
  }

  public void setAlarmLevel(AlarmLevel alarmLevel) {
    this.alarmLevel = alarmLevel;
  }

  public String getAlarmDescription() {
    return alarmDescription;
  }

  public void setAlarmDescription(String alarmDescription) {
    this.alarmDescription = alarmDescription;
  }

  public String getAlarmObject() {
    return alarmObject;
  }

  public void setAlarmObject(String alarmObject) {
    this.alarmObject = alarmObject;
  }

  public AlarmOper getOper() {
    return oper;
  }

  public void setOper(AlarmOper oper) {
    this.oper = oper;
  }

  @Override
  public String toString() {
    return "AlarmMessageData [alarmObject=" + alarmObject + ", alarmName=" + alarmName
        + ", alarmLevel="
        + alarmLevel + ", oper=" + oper + ", alarmDescription=" + alarmDescription + "]";
  }

  public enum AlarmOper {
    APPEAR, DISAPPEAR
  }

}
