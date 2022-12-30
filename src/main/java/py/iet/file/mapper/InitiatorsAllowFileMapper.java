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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitiatorsAllowFileMapper implements IetFileMapper {
  private static final Logger logger = LoggerFactory.getLogger(InitiatorsAllowFileMapper.class);
  public String filePath;
  private Map<String, List<String>> initiatorAllowTable = new HashMap<String, List<String>>();

  @Override
  public boolean load() {
    if (initiatorAllowTable != null) {
      initiatorAllowTable.clear();
    }

    try {
      BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
      String readLine = null;
      while ((readLine = reader.readLine()) != null) {
        readLine = readLine.trim();

        Pattern commentPattern = Pattern.compile("#");
        Matcher commentMatcher = commentPattern.matcher(readLine);
        if (commentMatcher.find(0)) {
          continue;
        }

        Pattern allAllowPattern = Pattern.compile("ALL\\s+ALL");
        Matcher allAllowMatcher = allAllowPattern.matcher(readLine);
        if (allAllowMatcher.find(0)) {
          continue;
        }

        if (readLine.split("\\s+").length == 2) {
          String targetName = readLine.split("\\s+")[0];

          String allowInitiatorsStr = readLine.split("\\s+")[1];
          String[] allowInitiatorArray = allowInitiatorsStr.split(",\\s*");

          List<String> allowInitiatorList = new ArrayList<String>();
          for (String allowInitiator : allowInitiatorArray) {
            allowInitiatorList.add(allowInitiator);
          }
          initiatorAllowTable.put(targetName, allowInitiatorList);
        }
      }

      reader.close();
    } catch (Exception e) {
      logger.error("Caught an exception when load file {}", filePath, e);
      return false;
    }

    return true;
  }

  @Override
  public synchronized boolean flush() {
    try {
      if (initiatorAllowTable == null) {
        initiatorAllowTable = new HashMap<String, List<String>>();
      }

      List<String> commentList = new ArrayList<String>();
      if (new File(filePath).exists()) {
        BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
        try {
          String readLine = null;
          while ((readLine = reader.readLine()) != null) {
            Pattern commentPattern = Pattern.compile("\\s*#");
            Matcher commentMatcher = commentPattern.matcher(readLine);
            if (commentMatcher.find(0)) {
              commentList.add(readLine);
            }
          }
        } finally {
          reader.close();
        }
      }

      BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(filePath));
      try {
        for (String comment : commentList) {
          bufferWriter.write(comment);
          bufferWriter.newLine();
        }

        for (String targetName : initiatorAllowTable.keySet()) {
          List<String> allowInitiatorList = initiatorAllowTable.get(targetName);
          if (allowInitiatorList == null || allowInitiatorList.isEmpty()) {
            logger.warn("No access rules for target {}", targetName);
            continue;
          }
          StringBuilder allowInitiatorsStrBuilder = new StringBuilder();
          for (String allowInitiator : allowInitiatorList) {
            allowInitiatorsStrBuilder.append(String.format("%s,", allowInitiator));
          }
          allowInitiatorsStrBuilder.deleteCharAt(allowInitiatorsStrBuilder.length() - 1);

          bufferWriter
              .write(String.format("%s %s", targetName, allowInitiatorsStrBuilder.toString()));
          bufferWriter.newLine();
        }
        bufferWriter.flush();
      } finally {
        bufferWriter.close();
      }
    } catch (Exception e) {
      logger.error("Caught an exception when flush initiators allow table to {}", filePath, e);
      return false;
    }

    return true;
  }

  public Map<String, List<String>> getInitiatorAllowTable() {
    return initiatorAllowTable;
  }

  public void setInitiatorAllowTable(Map<String, List<String>> initiatorAllowTable) {
    this.initiatorAllowTable = initiatorAllowTable;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

}
