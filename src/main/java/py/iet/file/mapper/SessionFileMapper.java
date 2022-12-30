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

package py.iet.file.mapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionFileMapper implements IetFileMapper {
  private static final Logger logger = LoggerFactory.getLogger(SessionFileMapper.class);
  private String filePath;
  private List<Session> sessionList = new ArrayList<Session>();

  @Override
  public boolean load() {
    if (sessionList != null) {
      sessionList.clear();
    }

    try {
      Session session = null;
      Initiator initiator = null;
      BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
      String readLine = null;
      while ((readLine = reader.readLine()) != null) {
        readLine = readLine.trim();

        Pattern targetParttern = Pattern.compile("tid:[0-9]+");
        Matcher targetMatcher = targetParttern.matcher(readLine);
        if (targetMatcher.find(0)) {
          session = new Session();
          addToSessionList(session);
          for (String sessionProp : readLine.split("\\s+")) {
            if (sessionProp.contains("tid")) {
              int tid = Integer.parseInt(sessionProp.split(":")[1]);
              session.setTid(tid);
            }

            if (sessionProp.contains("name")) {
              String targetName = sessionProp.substring(sessionProp.indexOf(":") + 1);
              session.setTargetName(targetName);
            }
          }

          continue;
        }

        Pattern sessionParttern = Pattern.compile("sid:[0-9]+");
        Matcher sessionMatcher = sessionParttern.matcher(readLine);
        if (sessionMatcher.find(0)) {
          initiator = new Initiator();
          session.addToInitiatorList(initiator);
          for (String sessionProp : readLine.split("\\s+")) {
            if (sessionProp.contains("sid")) {
              long sid = Long.parseLong(sessionProp.split(":")[1]);
              initiator.setSid(sid);
            }
          }
        }

        Pattern initiatorPattern = Pattern.compile("cid:[0-9]+");
        Matcher initiatorMatcher = initiatorPattern.matcher(readLine);
        if (initiatorMatcher.find(0)) {
          for (String initiatorProp : readLine.split("\\s+")) {
            if (initiatorProp.contains("cid")) {
              int cid = Integer.parseInt(initiatorProp.split(":")[1]);
              initiator.setCid(cid);
            }

            if (initiatorProp.contains("ip")) {
              int indexOfFirstColon;
              InetAddress inetAddr;
              String ip;

              indexOfFirstColon = initiatorProp.indexOf(":");
              ip = initiatorProp.substring(indexOfFirstColon + 1);
              inetAddr = InetAddress.getByName(ip);
              initiator.setIp(inetAddr.getHostAddress());
            }
          }
          continue;
        }
      }
    } catch (Exception e) {
      logger.error("Caught an exception when load file {}", filePath);
      return false;
    }

    return true;
  }

  public void addToSessionList(Session session) {
    if (sessionList == null) {
      sessionList = new ArrayList<Session>();
    }

    sessionList.add(session);
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public List<Session> getSessionList() {
    return sessionList;
  }

  public void setSessionList(List<Session> sessionList) {
    this.sessionList = sessionList;
  }

  @Override
  public boolean flush() {
    throw new NotImplementedException(
        String.format("%s hasn't implement the interface", getClass().getName()));
  }

  public static class Initiator {
    private int cid;

    private long sid;

    private String ip;

    public int getCid() {
      return cid;
    }

    public void setCid(int cid) {
      this.cid = cid;
    }

    public long getSid() {
      return sid;
    }

    public void setSid(long sid) {
      this.sid = sid;
    }

    public String getIp() {
      return ip;
    }

    public void setIp(String ip) {
      this.ip = ip;
    }

  }

  public static class Session {
    private int tid;

    private String targetName;

    private List<Initiator> initiatorList;

    public int getTid() {
      return tid;
    }

    public void setTid(int tid) {
      this.tid = tid;
    }

    public String getTargetName() {
      return targetName;
    }

    public void setTargetName(String targetName) {
      this.targetName = targetName;
    }

    public List<Initiator> getInitiatorList() {
      return initiatorList;
    }

    public void setInitiatorList(List<Initiator> initiatorList) {
      this.initiatorList = initiatorList;
    }

    public void addToInitiatorList(Initiator initiator) {
      if (initiatorList == null) {
        initiatorList = new ArrayList<Initiator>();
      }

      initiatorList.add(initiator);
    }

  }
}
