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

public class DtoSenderLog {
  private long id;
  private String userName;
  private String jobNum;
  private String type;
  private boolean result;
  private long sendTime;
  private String eventType;
  private String sendNotice;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getJobNum() {
    return jobNum;
  }

  public void setJobNum(String jobNum) {
    this.jobNum = jobNum;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isResult() {
    return result;
  }

  public void setResult(boolean result) {
    this.result = result;
  }

  public long getSendTime() {
    return sendTime;
  }

  public void setSendTime(long sendTime) {
    this.sendTime = sendTime;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getSendNotice() {
    return sendNotice;
  }

  public void setSendNotice(String sendNotice) {
    this.sendNotice = sendNotice;
  }
}
