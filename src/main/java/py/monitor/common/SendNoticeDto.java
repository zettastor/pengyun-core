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

import java.util.List;
import java.util.Map;

public class SendNoticeDto {
  private long id;
  private String type;
  private List<String> receiverList;
  private String message;
  private String appId;
  private Map<String, Object> extension;
  private String noticeLevel;
  private boolean emailHtmlFlag;
  private MonitorPlatformDataDto monitorPlatformDataDto;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<String> getReceiverList() {
    return receiverList;
  }

  public void setReceiverList(List<String> receiverList) {
    this.receiverList = receiverList;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public Map<String, Object> getExtension() {
    return extension;
  }

  public void setExtension(Map<String, Object> extension) {
    this.extension = extension;
  }

  public String getNoticeLevel() {
    return noticeLevel;
  }

  public void setNoticeLevel(String noticeLevel) {
    this.noticeLevel = noticeLevel == null ? "COMMON" : noticeLevel;
  }

  public boolean isEmailHtmlFlag() {
    return emailHtmlFlag;
  }

  public void setEmailHtmlFlag(boolean emailHtmlFlag) {
    this.emailHtmlFlag = emailHtmlFlag;
  }

  public MonitorPlatformDataDto getMonitorPlatformDataDto() {
    return monitorPlatformDataDto;
  }

  public void setMonitorPlatformDataDto(
      MonitorPlatformDataDto monitorPlatformDataDto) {
    this.monitorPlatformDataDto = monitorPlatformDataDto;
  }

  @Override
  public String toString() {
    return "SendNoticeDTO{" + "type='" + type + '\''
        + ", message='" + message + '\''
        + ", appId='" + appId + '\''
        + ", extension='" + extension + '\''
        + ", noticeLevel='" + noticeLevel + '\''
        + ", MonitorPlatformDataDTO={"
        + "sendMessage='" + monitorPlatformDataDto.isSendMessage() + '\''
        + ", alarmSource='" + monitorPlatformDataDto.getAlarmSource() + '\''
        + ", alarmMetric='" + monitorPlatformDataDto.getAlarmMetric() + '\''
        + ", alarmTag='" + monitorPlatformDataDto.getAlarmTag() + '\''
        + ", alarmInfo='" + monitorPlatformDataDto.getAlarmInfo() + '\''
        + ", alarmLevel='" + monitorPlatformDataDto.getAlarmLevel() + '\''
        + ", eventType='" + monitorPlatformDataDto.getEventType() + '\''
        + ", alarmTime=" + monitorPlatformDataDto.getAlarmTime()
        + ", sms='" + monitorPlatformDataDto.getSms() + '\''
        + ", email='" + monitorPlatformDataDto.getEmail() + '\''
        + ", phone=" + monitorPlatformDataDto.getPhone()
        + ", system='" + monitorPlatformDataDto.getSystem() + '\''
        + ", alarmSysCode='" + monitorPlatformDataDto.getAlarmSysCode() + '\''
        + ", alarmName='" + monitorPlatformDataDto.getAlarmName() + '\''
        + ", alarmValue=" + monitorPlatformDataDto.getAlarmValue()
        + ", alarmThreshold=" + monitorPlatformDataDto.getAlarmThreshold() + "}}";
  }
}
