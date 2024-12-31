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
