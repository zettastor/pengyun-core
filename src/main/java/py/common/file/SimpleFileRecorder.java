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

package py.common.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * record a string in a line.
 */
public class SimpleFileRecorder implements FileRecorder {
  private static final Logger logger = LoggerFactory.getLogger(SimpleFileRecorder.class);

  private File file;

  public SimpleFileRecorder(String fileName) {
    file = new File(fileName);
  }

  @Override
  public List<String> records() {
    List<String> lines;
    try {
      lines = FileUtils.readLines(file, ENCODING);
    } catch (IOException e) {
      logger.error("failed read file:[{}]", file);
      return new ArrayList<>();
    }
    return lines;
  }

  @Override
  public boolean isEmpty() {
    return records().size() == 0 || records().stream().allMatch(r -> r.equals(""));
  }

  @Override
  public boolean contains(String record) {
    if (!fileExist()) {
      logger.warn("file:[{}] does not exist, record :[{}] is not in", file.getName(), record);
      return false;
    }

    return records().contains(record);
  }

  @Override
  public boolean add(String record) {
    if (contains(record)) {
      logger.warn("file:[{}], record :[{}] is in", file.getName(), record);
      return true;
    }

    try {
      FileUtils.writeLines(file, ENCODING, Collections.singletonList(record), true);
      logger.warn("add record:[{}] to file", record, file);
      return true;
    } catch (IOException e) {
      logger.error("failed to add record:[{}] to file:[{}]", record, file);
      return false;
    }
  }

  @Override
  public boolean remove(String record) {
    List<String> lines = records();
    if (!lines.remove(record)) {
      logger.warn("file:[{}], record :[{}] is not in", file.getName(), record);
      return true;
    } else {
      try {
        FileUtils.writeLines(file, ENCODING, lines);
        logger.warn("remove record :[{}] from file ", record, file);
        return true;
      } catch (IOException e) {
        logger.error("failed to remove record :[{}] from file:[{}]", record, file);
        return false;
      }
    }
  }

  @Override
  public boolean fileExist() {
    return file.exists();
  }

}
