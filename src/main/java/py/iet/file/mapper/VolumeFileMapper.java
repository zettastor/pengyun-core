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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.iet.file.mapper.ConfigurationFileMapper.Lun;
import py.iet.file.mapper.ConfigurationFileMapper.Target;

public class VolumeFileMapper implements IetFileMapper {
  private static final Logger logger = LoggerFactory.getLogger(VolumeFileMapper.class);

  private String filePath;
  private List<Volume> volumeList = new ArrayList<Volume>();

  @Override
  public boolean load() {
    if (volumeList != null) {
      volumeList.clear();
    }

    try {
      Volume volume = null;
      BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
      String readLine = null;
      while ((readLine = reader.readLine()) != null) {
        readLine = readLine.trim();

        Pattern volumePattern = Pattern.compile("tid:[0-9]+", Pattern.CASE_INSENSITIVE);
        Matcher volumeMatcher = volumePattern.matcher(readLine);
        if (volumeMatcher.find(0)) {
          volume = new Volume();
          for (String volumeProp : readLine.split("\\s+")) {
            if (volumeProp.contains("tid")) {
              int tid = Integer.parseInt(volumeProp.split(":")[1]);
              volume.setTid(tid);
            }

            if (volumeProp.contains("name")) {
              String targetName = volumeProp.substring(volumeProp.indexOf(":") + 1);
              volume.setTargetName(targetName);
            }
          }
          addToVolumeList(volume);
          continue;
        }

        Pattern lunPattern = Pattern.compile("lun:[0-9]+", Pattern.CASE_INSENSITIVE);
        Matcher lunMatcher = lunPattern.matcher(readLine);
        if (lunMatcher.find(0)) {
          Lun lun = new Lun();
          for (String lunProp : readLine.split("\\s+")) {
            if (lunProp.contains("lun")) {
              int lunIndex = Integer.parseInt(lunProp.split(":")[1]);
              lun.setIndex(lunIndex);
            }

            if (lunProp.contains("path")) {
              String path = lunProp.split(":")[1];
              lun.setPath(path);
            }

            if (lunProp.contains("type")) {
              String type = lunProp.split(":")[1];
              lun.setType(type);
            }

            if (lunProp.contains("blocks") && lunProp.split(":")[0].equals("blocks")) {
              long blocks = Long.parseLong(lunProp.split(":")[1]);
              lun.setBlocks(blocks);
            }
            if (lunProp.contains("blocksize")) {
              long blocksize = Long.parseLong(lunProp.split(":")[1]);
              lun.setBlockSize(blocksize);
            }
          }

          volume.addToLunList(lun);
        }
      }
      reader.close();
    } catch (Exception e) {
      logger.error("Caught an exception when load file {}", filePath);
      return false;
    }

    return true;
  }

  public void addToVolumeList(Volume volume) {
    if (volumeList == null) {
      volumeList = new ArrayList<Volume>();
    }

    volumeList.add(volume);
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public List<Volume> getVolumeList() {
    return volumeList;
  }

  public void setVolumeList(List<Volume> volumeList) {
    this.volumeList = volumeList;
  }

  @Override
  public boolean flush() {
    throw new NotImplementedException(
        String.format("%s hasn't implement the interface", getClass().getName()));
  }

  public static class Volume extends Target {
    private int tid;

    public int getTid() {
      return tid;
    }

    public void setTid(int tid) {
      this.tid = tid;
    }

    @Override
    public String toString() {
      return "Volume [tid=" + tid + "]";
    }
  }
}
