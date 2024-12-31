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