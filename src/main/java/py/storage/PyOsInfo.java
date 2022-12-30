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

package py.storage;

public class PyOsInfo {
  private static Os os = Os.OTHER;

  public static Os getOs() {
    String osName = System.getProperty("os.name");
    if (osName == null) {
      os = Os.OTHER;
    }
    osName = osName.toLowerCase();
    if (osName.contains("windows")) {
      os = Os.WINDOWS;
    } else if (osName.contains("linux")) {
      os = Os.LINUX;
    } else if (osName.contains("mac os")) {
      os = Os.MAC;
    } else if (osName.contains("sun os")
        || osName.contains("sunos")
        || osName.contains("solaris")) {
      os = Os.UNIX;
    } else {
      os = Os.OTHER;
    }
    os.setVersion(System.getProperty("os.version"));
    return os;
  }

  public enum Os {
    WINDOWS,
    LINUX,
    UNIX,
    MAC,
    OTHER;

    private String version;

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
    }
  }
}
