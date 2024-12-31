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

package py.debug;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicParamConfig {
  public static final String SEPARATOR = "##";
  public static final String LOG_LEVEL = "log.level";
  private static final Logger logger = LoggerFactory.getLogger(DynamicParamConfig.class);

  private static DynamicParamConfig instance = null;
  private Map<String, String> dynamicParameters;

  private DynamicParamConfig() {
    dynamicParameters = new ConcurrentHashMap<>();
    dynamicParameters.put(LOG_LEVEL, LogManager.getRootLogger().getLevel().toString());
  }

  public static DynamicParamConfig getInstance() {
    if (instance == null) {
      synchronized (DynamicParamConfig.class) {
        if (instance == null) {
          instance = new DynamicParamConfig();
        }
      }
    }
    return instance;
  }

  public static void main(String[] args) {
    DynamicParamConfig dynamicParamConfig = new DynamicParamConfig();
    dynamicParamConfig.setParameter(LOG_LEVEL,
        Level.WARN.toString() + SEPARATOR + "py.service.debug.DynamicParamConfig");
    dynamicParamConfig.test();

    dynamicParamConfig.setParameter(LOG_LEVEL, Level.DEBUG.toString());
    dynamicParamConfig.test();
  }

  public void setParameter(String name, String value) {
    switch (name.toLowerCase()) {
      case LOG_LEVEL:
        setLogLevelDynamically(value);
        break;
      default:
        logger.debug("can't set this parameter " + name);
    }
  }

  private void setLogLevelDynamically(String value) {
    String logLevel = null;
    String className = null;
    if (value.contains(SEPARATOR)) {
      String[] split = value.split(SEPARATOR);
      logLevel = split[0];
      className = split[1];
    }
    if (className != null) {
      try {
        LogManager.getLogger(Class.forName(className)).setLevel(Level.toLevel(logLevel));
        dynamicParameters.put(LOG_LEVEL + "." + className, logLevel);
        logger.warn("the {} log level is set to {} successfully.", className, logLevel);
      } catch (ClassNotFoundException e) {
        logger.warn("cannot reflect the Class, className is: {}", className);
      }
    } else {
      logger.debug("level change to " + value);
      Enumeration currentLoggers = LogManager.getRootLogger().getLoggerRepository()
          .getCurrentLoggers();
      while (currentLoggers.hasMoreElements()) {
        ((org.apache.log4j.Logger) currentLoggers.nextElement()).setLevel(Level.toLevel(value));
      }

      dynamicParameters.entrySet().removeIf(next -> next.getKey().contains(LOG_LEVEL));
      dynamicParameters.put(LOG_LEVEL, value);
    }
  }

  public Map<String, String> getParameter() {
    return dynamicParameters;
  }

  public Map<String, String> getLogLevels() {
    Map<String, String> logLevels = new HashMap<>();
    for (Map.Entry<String, String> entry : dynamicParameters.entrySet()) {
      if (entry.getKey().contains(LOG_LEVEL)) {
        logLevels.put(entry.getKey(), entry.getValue());
      }
    }
    return logLevels;
  }

  public void test() {
    System.out.println("=======================");
    logger.trace("trace test");
    logger.debug("debug test");
    logger.info("info test");
    logger.warn("warn test");
    logger.error("error test");
  }
}
