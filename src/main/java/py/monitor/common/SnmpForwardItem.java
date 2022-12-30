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

public class SnmpForwardItem {
  private long id;
  private String trapServerip;
  private int trapServerport;
  private String community;
  private String snmpVersion;
  private String securityName;
  private String authProtocol;
  private String privProtocol;
  private String authKey;
  private String privKey;
  private String securityLevel;
  private boolean enable;
  private String nmsName;
  private int timeoutMs;
  private String nmsDescription;
  private String language;
  private String languageFormat;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getTrapServerip() {
    return trapServerip;
  }

  public void setTrapServerip(String trapServerip) {
    this.trapServerip = trapServerip;
  }

  public int getTrapServerport() {
    return trapServerport;
  }

  public void setTrapServerport(int trapServerport) {
    this.trapServerport = trapServerport;
  }

  public String getCommunity() {
    return community;
  }

  public void setCommunity(String community) {
    this.community = community;
  }

  public String getSnmpVersion() {
    return snmpVersion;
  }

  public void setSnmpVersion(String snmpVersion) {
    this.snmpVersion = snmpVersion;
  }

  public String getSecurityName() {
    return securityName;
  }

  public void setSecurityName(String securityName) {
    this.securityName = securityName;
  }

  public String getAuthProtocol() {
    return authProtocol;
  }

  public void setAuthProtocol(String authProtocol) {
    this.authProtocol = authProtocol;
  }

  public String getPrivProtocol() {
    return privProtocol;
  }

  public void setPrivProtocol(String privProtocol) {
    this.privProtocol = privProtocol;
  }

  public String getAuthKey() {
    return authKey;
  }

  public void setAuthKey(String authKey) {
    this.authKey = authKey;
  }

  public String getPrivKey() {
    return privKey;
  }

  public void setPrivKey(String privKey) {
    this.privKey = privKey;
  }

  public String getSecurityLevel() {
    return securityLevel;
  }

  public void setSecurityLevel(String securityLevel) {
    this.securityLevel = securityLevel;
  }

  public boolean isEnable() {
    return enable;
  }

  public void setEnable(boolean enable) {
    this.enable = enable;
  }

  public String getNmsName() {
    return nmsName;
  }

  public void setNmsName(String nmsName) {
    this.nmsName = nmsName;
  }

  public int getTimeoutMs() {
    return timeoutMs;
  }

  public void setTimeoutMs(int timeoutMs) {
    this.timeoutMs = timeoutMs;
  }

  public String getNmsDescription() {
    return nmsDescription;
  }

  public void setNmsDescription(String nmsDescription) {
    this.nmsDescription = nmsDescription;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getLanguageFormat() {
    return languageFormat;
  }

  public void setLanguageFormat(String languageFormat) {
    this.languageFormat = languageFormat;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SnmpForwardItem that = (SnmpForwardItem) o;

    if (id != that.id) {
      return false;
    }
    if (trapServerport != that.trapServerport) {
      return false;
    }
    if (enable != that.enable) {
      return false;
    }
    if (timeoutMs != that.timeoutMs) {
      return false;
    }
    if (trapServerip != null ? !trapServerip.equals(that.trapServerip)
        : that.trapServerip != null) {
      return false;
    }
    if (community != null ? !community.equals(that.community) : that.community != null) {
      return false;
    }
    if (snmpVersion != null ? !snmpVersion.equals(that.snmpVersion) : that.snmpVersion != null) {
      return false;
    }
    if (securityName != null ? !securityName.equals(that.securityName)
        : that.securityName != null) {
      return false;
    }
    if (authProtocol != null ? !authProtocol.equals(that.authProtocol)
        : that.authProtocol != null) {
      return false;
    }
    if (privProtocol != null ? !privProtocol.equals(that.privProtocol)
        : that.privProtocol != null) {
      return false;
    }
    if (authKey != null ? !authKey.equals(that.authKey) : that.authKey != null) {
      return false;
    }
    if (privKey != null ? !privKey.equals(that.privKey) : that.privKey != null) {
      return false;
    }
    if (securityLevel != null ? !securityLevel.equals(that.securityLevel)
        : that.securityLevel != null) {
      return false;
    }
    if (nmsName != null ? !nmsName.equals(that.nmsName) : that.nmsName != null) {
      return false;
    }
    if (nmsDescription != null ? !nmsDescription.equals(that.nmsDescription)
        : that.nmsDescription != null) {
      return false;
    }
    if (language != null ? !language.equals(that.language) : that.language != null) {
      return false;
    }
    return languageFormat != null ? languageFormat.equals(that.languageFormat)
        : that.languageFormat == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (id ^ (id >>> 32));
    result = 31 * result + (trapServerip != null ? trapServerip.hashCode() : 0);
    result = 31 * result + trapServerport;
    result = 31 * result + (community != null ? community.hashCode() : 0);
    result = 31 * result + (snmpVersion != null ? snmpVersion.hashCode() : 0);
    result = 31 * result + (securityName != null ? securityName.hashCode() : 0);
    result = 31 * result + (authProtocol != null ? authProtocol.hashCode() : 0);
    result = 31 * result + (privProtocol != null ? privProtocol.hashCode() : 0);
    result = 31 * result + (authKey != null ? authKey.hashCode() : 0);
    result = 31 * result + (privKey != null ? privKey.hashCode() : 0);
    result = 31 * result + (securityLevel != null ? securityLevel.hashCode() : 0);
    result = 31 * result + (enable ? 1 : 0);
    result = 31 * result + (nmsName != null ? nmsName.hashCode() : 0);
    result = 31 * result + timeoutMs;
    result = 31 * result + (nmsDescription != null ? nmsDescription.hashCode() : 0);
    result = 31 * result + (language != null ? language.hashCode() : 0);
    result = 31 * result + (languageFormat != null ? languageFormat.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "SnmpForwardItem{" + "id=" + id + ", trapServerip='" + trapServerip + '\''
        + ", trapServerport="
        + trapServerport + ", community='" + community + '\'' + ", snmpVersion='" + snmpVersion
        + '\''
        + ", securityName='" + securityName + '\'' + ", authProtocol='" + authProtocol + '\''
        + ", privProtocol='" + privProtocol + '\'' + ", authKey='" + authKey + '\'' + ", privKey='"
        + privKey
        + '\'' + ", securityLevel='" + securityLevel + '\'' + ", enable=" + enable + ", NMSName='"
        + nmsName
        + '\'' + ", timeoutMs=" + timeoutMs + ", NMSDescription='" + nmsDescription + '\''
        + ", language='"
        + language + '\'' + ", languageFormat='" + languageFormat + '\'' + '}';
  }
}
