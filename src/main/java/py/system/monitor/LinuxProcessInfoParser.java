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

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LinuxProcessInfoParser {
  private static final Pattern STATUS_NAME_MATCHER = Pattern
      .compile("Name:\\s+(\\w+)", Pattern.MULTILINE);
  private static final Pattern STATUS_UID_MATCHER = Pattern
      .compile("Uid:\\s+(\\d+)\\s.*", Pattern.MULTILINE);
  private final String stat;
  private final String status;
  private final String cmdline;
  private final HashMap uids;
  private final String cwd;
  private final int nfds;
  private final int ntasks;

  public LinuxProcessInfoParser(String stat, String status, String cmdline, HashMap uids,
      String cwd, int nfds,
      int ntasks) {
    this.stat = stat;
    this.status = status;
    this.cmdline = cmdline;
    this.uids = uids;
    this.cwd = cwd;
    this.nfds = nfds;
    this.ntasks = ntasks;
  }

  public ProcessInfo parse() throws ParseException {
    int openParen = stat.indexOf("(");
    int closeParen = stat.lastIndexOf(")");
    if (openParen <= 1 || closeParen < 0 || closeParen > stat.length() - 2) {
      throw new ParseException(
          "Stat '" + stat + "' does not include expected parens around process name");
    }

    String[] statElements = stat.substring(closeParen + 2).split(" ");
    if (statElements.length < 13) {
      throw new ParseException("Stat '" + stat + "' contains fewer elements than expected");
    }

    String pidStr = stat.substring(0, openParen - 1);

    int pid;
    int parentPid;
    try {
      pid = Integer.parseInt(pidStr);
      parentPid = Integer.parseInt(statElements[1]);
    } catch (NumberFormatException e) {
      throw new ParseException("Unable to parse stat '" + stat + "'");
    }

    return new ProcessInfo(pid, parentPid, trim(cmdline),
        getFirstMatch(STATUS_NAME_MATCHER, status),
        (String) uids.get(getFirstMatch(STATUS_UID_MATCHER, status)), cwd, nfds, ntasks);
  }

  private String trim(String cmdline) {
    return cmdline.replace('\000', ' ').replace('\n', ' ');
  }

  public String getFirstMatch(Pattern pattern, String string) {
    try {
      Matcher matcher = pattern.matcher(string);
      matcher.find();
      return matcher.group(1);
    } catch (Exception e) {
      return "0";
    }
  }
}
