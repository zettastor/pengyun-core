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

package py.system.monitor;

public class ProcessInfo {
  private static final String PADDING = "                                                          "
      + "                         ";

  private int pid;
  private int parentPid;
  private String command;
  private String name;
  private String owner;
  private String cwd;
  private int nfds;
  private int ntasks;

  public ProcessInfo(int pid, int parentPid, String command, String name, String owner, String cwd,
      int nfds,
      int ntasks) {
    this.pid = pid;
    this.parentPid = parentPid;
    this.command = command;
    this.name = name;
    this.owner = owner;
    this.cwd = cwd;
    this.nfds = nfds;
    this.ntasks = ntasks;
  }

  private static String stringFormat(int intToFormat, int fieldSize) {
    return stringFormat(Integer.toString(intToFormat), fieldSize, true);
  }

  private static String stringFormat(String stringToFormat, int fieldSize) {
    return stringFormat(stringToFormat, fieldSize, false);
  }

  private static String stringFormat(String stringToFormat, int fieldSize, boolean rightJustify) {
    if (stringToFormat.length() >= fieldSize) {
      return stringToFormat.substring(0, fieldSize);
    } else {
      return rightJustify ? PADDING.substring(0, fieldSize - stringToFormat.length())
          + stringToFormat
          : stringToFormat + PADDING.substring(0, fieldSize - stringToFormat.length());
    }
  }

  public int getPid() {
    return pid;
  }

  public int getParentPid() {
    return parentPid;
  }

  public String getCommand() {
    return command;
  }

  public String getName() {
    return name;
  }

  public String getOwner() {
    return owner;
  }

  public String getCwd() {
    return cwd;
  }

  public int getnFds() {
    return nfds;
  }

  public int getNtasks() {
    return ntasks;
  }

  @Override
  public String toString() {
    return stringFormat(pid, 5) + " " + stringFormat(name, 10) + " " + stringFormat(parentPid, 5)
        + " "
        + stringFormat(owner, 10) + " " + stringFormat(command, 23) + " " + stringFormat(nfds, 8)
        + " "
        + stringFormat(ntasks, 8) + " " + cwd;
  }
}
