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
