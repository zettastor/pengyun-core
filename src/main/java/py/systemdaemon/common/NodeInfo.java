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

package py.systemdaemon.common;

import java.util.ArrayList;
import java.util.List;

public class NodeInfo {
  public static final String NETCARD_IFACE_IP_SEPARATOR = ": ";
  public static final String NETCARD_IP_SEPARATOR = "##";

  public static StringBuilder makeNetCardInfo(StringBuilder lastNetCardInfo, String iface,
      String ip) {
    StringBuilder buf;
    if (lastNetCardInfo != null) {
      buf = lastNetCardInfo;
      if (buf.length() != 0) {
        buf.append(NodeInfo.NETCARD_IP_SEPARATOR);
      }
    } else {
      buf = new StringBuilder();
    }

    buf.append(iface).append(NodeInfo.NETCARD_IFACE_IP_SEPARATOR).append(ip);
    return buf;
  }

  public static List<String> parseNetCardInfoToGetIps(String netCardInfo) {
    List<String> ipList = new ArrayList<>();
    String[] netCardArry = netCardInfo.split(NodeInfo.NETCARD_IP_SEPARATOR);
    for (String netCard : netCardArry) {
      if (netCard == null || netCard.isEmpty()) {
        continue;
      }

      String[] netCardAttrArry = netCard.split(NodeInfo.NETCARD_IFACE_IP_SEPARATOR);
      if (netCardAttrArry.length != 2) {
        continue;
      }

      ipList.add(netCardAttrArry[1]);
    }
    return ipList;
  }

  public static StringBuilder makeInfoGroup(StringBuilder lastInfo, String info) {
    StringBuilder buf;
    if (lastInfo != null) {
      buf = lastInfo;
      if (buf.length() != 0) {
        buf.append(NodeInfo.NETCARD_IP_SEPARATOR);
      }
    } else {
      buf = new StringBuilder();
    }
    buf.append(info);
    return buf;
  }

  public static List<String> parseInfoGroup(String infoGroup) {
    List<String> infoList = new ArrayList<>();
    String[] infoArry = infoGroup.split(NodeInfo.NETCARD_IP_SEPARATOR);
    for (String info : infoArry) {
      if (info == null || info.isEmpty()) {
        continue;
      }
      infoList.add(info);
    }
    return infoList;
  }

}
