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
