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
