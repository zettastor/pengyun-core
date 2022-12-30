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

public class MonitorPlatformDataDto {
  private boolean sendMessage;
  private String alarmSource;
  private String alarmMetric;
  private String alarmTag;
  private String alarmInfo;
  private String alarmLevel;
  private String eventType;
  private String alarmTime;
  private String sms;
  private String email;
  private String link;
  private String phone;
  private String system;
  private String alarmSysCode;
  private String alarmName;
  private String alarmValue;
  private String alarmThreshold;

  public boolean isSendMessage() {
    return sendMessage;
  }

  public void setSendMessage(boolean sendMessage) {
    this.sendMessage = sendMessage;
  }

  public String getAlarmSource() {
    return alarmSource;
  }

  public void setAlarmSource(String alarmSource) {
    this.alarmSource = alarmSource;
  }

  public String getAlarmMetric() {
    return alarmMetric;
  }

  public void setAlarmMetric(String alarmMetric) {
    this.alarmMetric = alarmMetric;
  }

  public String getAlarmTag() {
    return alarmTag;
  }

  public void setAlarmTag(String alarmTag) {
    this.alarmTag = alarmTag;
  }

  public String getAlarmInfo() {
    return alarmInfo;
  }

  public void setAlarmInfo(String alarmInfo) {
    this.alarmInfo = alarmInfo;
  }

  public String getAlarmLevel() {
    return alarmLevel;
  }

  public void setAlarmLevel(String alarmLevel) {
    this.alarmLevel = alarmLevel;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getAlarmTime() {
    return alarmTime;
  }

  public void setAlarmTime(String alarmTime) {
    this.alarmTime = alarmTime;
  }

  public String getSms() {
    return sms;
  }

  public void setSms(String sms) {
    this.sms = sms;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getSystem() {
    return system;
  }

  public void setSystem(String system) {
    this.system = system;
  }

  public String getAlarmSysCode() {
    return alarmSysCode;
  }

  public void setAlarmSysCode(String alarmSysCode) {
    this.alarmSysCode = alarmSysCode;
  }

  public String getAlarmName() {
    return alarmName;
  }

  public void setAlarmName(String alarmName) {
    this.alarmName = alarmName;
  }

  public String getAlarmValue() {
    return alarmValue;
  }

  public void setAlarmValue(String alarmValue) {
    this.alarmValue = alarmValue;
  }

  public String getAlarmThreshold() {
    return alarmThreshold;
  }

  public void setAlarmThreshold(String alarmThreshold) {
    this.alarmThreshold = alarmThreshold;
  }
}