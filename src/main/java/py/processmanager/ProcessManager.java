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

package py.processmanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import py.processmanager.exception.UnableToStartServiceException;
import py.processmanager.utils.PmUtils;

public class ProcessManager {
  public static final String KW_DISABLE = "pm_disabled";

  private static final Logger logger = Logger.getLogger(ProcessManager.class);

  private String launcherPath;
  private boolean disabled = false;
  private List<String> launcherParams = new ArrayList<String>();

  public static void main(String[] args) throws IOException {
    if (args == null || args.length == 0) {
      logger.error("one argument containing service command is required, but cannot get it");
      System.exit(-1);
    }

    ProcessManager processManager = new ProcessManager();

    String launcherPath = args[0];
    processManager.setLauncherPath(launcherPath);
    logger.info(launcherPath);
    for (int i = 2; i < args.length; i++) {
      String arg = args[i];

      if (ProcessManager.KW_DISABLE.equals(arg.toLowerCase())) {
        processManager.setDisabled(true);
        continue;
      }

      processManager.addParams(arg);
    }

    try {
      String mutexTargetDir = args[1];
      if (ProcessManagerMutex.checkIfAlreadyRunning(mutexTargetDir)) {
        processManager.startService();
      } else {
        logger.warn("exit due to the same process is processing ");
        System.exit(1);
      }

    } catch (Exception e) {
      logger.error("Caught an exception", e);
      System.exit(1);
    }

  }

  public String getLauncherPath() {
    return launcherPath;
  }

  public void setLauncherPath(String launcherPath) {
    this.launcherPath = launcherPath;
  }

  public void addParams(String param) {
    launcherParams.add(param);
  }

  public List<String> getLauncherParams() {
    return launcherParams;
  }

  public boolean isDisabled() {
    return disabled;
  }

  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  public void startService() throws UnableToStartServiceException {
    if (launcherPath == null) {
      logger.error("unable to start service due to launcherPath is not specified");
      throw new UnableToStartServiceException();
    }

    File launcher = new File(launcherPath);
    if (!launcher.exists()) {
      logger.error("unable to start service due to launcher path doesn't exist");
      throw new UnableToStartServiceException();
    }

    String serviceRunningPath = launcher.getParentFile().getParentFile().getAbsolutePath();

    boolean alreadyBackupPid = false;
    try {
      Pmdb pmdb = Pmdb.build(Paths.get(serviceRunningPath));
      String backupStr = String.valueOf(PmUtils.getCurrentProcessPid());
      alreadyBackupPid = pmdb.save(Pmdb.PM_PID_NAME, backupStr);
    } catch (Exception e) {
      logger.error("Caught an exception", e);
    }
    if (!alreadyBackupPid) {
      logger.error("cannot backup my process manager pid into a file");
      throw new UnableToStartServiceException();
    }

    logger.debug("Process manager start to do its job, proguard service in " + serviceRunningPath
        + " running ...");
    while (true) {
      try {
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append(launcherPath);
        if (launcherParams.size() > 0) {
          for (String launcherParam : launcherParams) {
            commandBuilder.append(" " + launcherParam);
          }
        }

        String command = commandBuilder.toString();

        Process process = Runtime.getRuntime().exec(command, null, new File(serviceRunningPath));
        process.waitFor();

        if (disabled) {
          logger.warn("Disabled flag detected, process manager exit");
          break;
        }

      } catch (Exception e) {
        logger.warn("Caught an exception when process manager start service", e);
      }
    }
  }
}
