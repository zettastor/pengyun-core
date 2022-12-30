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

public class SmtpItem {
  boolean enable;
  private long id;
  private String userName;
  private String password;
  private int smtpPort;
  private String encryptType;
  private String subject;
  private String smtpServer;
  private String contentType;

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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getSmtpServer() {
    return smtpServer;
  }

  public void setSmtpServer(String smtpServer) {
    this.smtpServer = smtpServer;
  }

  public int getSmtpPort() {
    return smtpPort;
  }

  public void setSmtpPort(int smtpPort) {
    this.smtpPort = smtpPort;
  }

  public String getEncryptType() {
    return encryptType;
  }

  public void setEncryptType(String encryptType) {
    this.encryptType = encryptType;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public boolean isEnable() {
    return enable;
  }

  public void setEnable(boolean enable) {
    this.enable = enable;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SmtpItem smtpItem = (SmtpItem) o;

    if (id != smtpItem.id) {
      return false;
    }
    if (!userName.equals(smtpItem.userName)) {
      return false;
    }
    return smtpServer.equals(smtpItem.smtpServer);
  }

  @Override
  public int hashCode() {
    int result = (int) (id ^ (id >>> 32));
    result = 31 * result + userName.hashCode();
    result = 31 * result + smtpServer.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "SmtpItem{"
        + "id=" + id
        + ", userName='" + userName + '\''
        + ", password='" + password + '\''
        + ", smtpPort=" + smtpPort
        + ", encryptType='" + encryptType + '\''
        + ", subject='" + subject + '\''
        + ", smtpServer='" + smtpServer + '\''
        + ", contentType='" + contentType + '\''
        + ", enable=" + enable
        + '}';
  }

}
