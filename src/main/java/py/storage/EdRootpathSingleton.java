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

package py.storage;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.PyService;

public class EdRootpathSingleton {
  public static final String EVENT_DATA_PATH_PREFIX = "EventData";
  public static final String RECORD_POSITION_PATH_PREFIX = "RecordPosition";
  private static final Logger logger = LoggerFactory.getLogger(EdRootpathSingleton.class);
  private static volatile EdRootpathSingleton instance;

  private String rootPath;

  private EdRootpathSingleton() {
  }

  public static EdRootpathSingleton getInstance() {
    if (instance == null) {
      synchronized (EdRootpathSingleton.class) {
        if (instance == null) {
          instance = new EdRootpathSingleton();
        }
      }
    }
    return instance;
  }

  public String getRootPath() {
    return rootPath;
  }

  public void setRootPath(String rootPath) {
    logger.info("set event data root path:{}", rootPath);
    this.rootPath = rootPath;
  }

  public Path generateEventDataPath(String pyService) {
    Validate.notNull(getRootPath());
    Validate.notNull(pyService);
    logger.info("generate event data root path:{} with PyService:{}", getRootPath(), pyService);
    return Paths.get(getRootPath(), EVENT_DATA_PATH_PREFIX, pyService);
  }

  public Path generateRecordPath(PyService pyService) {
    Validate.notNull(getRootPath());
    Validate.notNull(pyService.getServiceName());
    logger.info("generate record data root path:{}", getRootPath());
    return Paths.get(getRootPath(), RECORD_POSITION_PATH_PREFIX, pyService.getServiceName());
  }

  public Path generateRecordPath(String applicationName) {
    Validate.notNull(applicationName);
    logger.info("generate record data root path:{}", getRootPath());
    return Paths.get(getRootPath(), RECORD_POSITION_PATH_PREFIX, applicationName);
  }
}
