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
