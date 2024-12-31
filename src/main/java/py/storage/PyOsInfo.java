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
