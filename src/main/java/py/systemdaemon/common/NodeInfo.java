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
