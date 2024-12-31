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
