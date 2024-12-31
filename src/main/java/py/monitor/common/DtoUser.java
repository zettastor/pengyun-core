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

public class DtoUser {
  private long id;
  private String userName;
  private String jobNum;
  private String sms;
  private String email;
  private String link;
  private String description;

  private boolean enableSms;
  private boolean enableEmail;
  private boolean enableLink;
  private boolean flag;

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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isEnableSms() {
    return enableSms;
  }

  public void setEnableSms(boolean enableSms) {
    this.enableSms = enableSms;
  }

  public boolean isEnableEmail() {
    return enableEmail;
  }

  public void setEnableEmail(boolean enableEmail) {
    this.enableEmail = enableEmail;
  }

  public boolean isEnableLink() {
    return enableLink;
  }

  public void setEnableLink(boolean enableLink) {
    this.enableLink = enableLink;
  }

  public boolean isFlag() {
    return flag;
  }

  public void setFlag(boolean flag) {
    this.flag = flag;
  }

  @Override
  public String toString() {
    String dtoHead = "DTOUser{";
    String dtoName = "userName='" + userName + '\'';
    String dtoNum = null != jobNum ? ", jobNum='" + jobNum + '\'' : "";
    String dtoSms = null != sms ? ", sms='" + sms + '\'' : "";
    String dtoLink = null != link ? ", link='" + link + '\'' : "";
    String dtoDescription = null != description ? ", description='" + description + '\'' : "";
    String dtoEnableSms = enableSms ? ", enableSms=" + enableSms : "";
    String dtoEnableEmail = enableEmail ? ", enableEmail=" + enableEmail : "";
    String dtoEnableLink = enableLink ? ", enableLink=" + enableLink : "";
    String dtoEnd = "}";

    return dtoHead + dtoName + dtoNum + dtoSms + dtoLink + dtoDescription + dtoEnableSms
        + dtoEnableEmail + dtoEnableLink + dtoEnd;
  }
}
