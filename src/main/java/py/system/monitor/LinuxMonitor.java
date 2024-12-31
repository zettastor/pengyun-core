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

package py.system.monitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinuxMonitor implements Monitor {
  private static final Logger logger = LoggerFactory.getLogger(LinuxMonitor.class);

  private static final Pattern PID_PATTERN = Pattern.compile("([\\d]*).*");

  private static final Pattern DISTRIBUTION = Pattern
      .compile("DISTRIB_DESCRIPTION=\"(.*)\"", Pattern.MULTILINE);

  private FileUtils fileUtils;

  LinuxMonitor(FileUtils fileUtils) {
    this.fileUtils = fileUtils;
  }

  public LinuxMonitor() {
    fileUtils = new FileUtils();
  }

  @Override
  public String osName() {
    String distribution = fileUtils.runRegexOnFile(DISTRIBUTION, "/etc/lsb-release");
    if (null == distribution) {
      return System.getProperty("os.name");
    }
    return distribution;
  }

  @Override
  public int currentPid() {
    String pid = fileUtils.runRegexOnFile(PID_PATTERN, "/proc/self/stat");
    return Integer.parseInt(pid);
  }

  @Override
  public ProcessInfo[] processTable() {
    ArrayList<ProcessInfo> processTable = new ArrayList<>();
    final String[] pids = fileUtils.pidsFromProcFilesystem();
    for (int i = 0; i < pids.length; i++) {
      ProcessInfo processInfo = processInfo(Integer.valueOf(pids[i]));

      if (processInfo != null) {
        processTable.add(processInfo);
      }
    }
    return processTable.toArray(new ProcessInfo[processTable.size()]);
  }

  @Override
  public void killProcess(int pid) {
    try {
      Process process = Runtime.getRuntime().exec("kill -9 " + pid);
      process.waitFor();
    } catch (Exception e) {
      throw new RuntimeException("Could not kill process id " + pid, e);
    }
  }

  @Override
  public ProcessInfo processInfo(int pid) {
    try {
      String stat = fileUtils.slurp("/proc/" + pid + "/stat");
      String status = fileUtils.slurp("/proc/" + pid + "/status");
      String cmdline = fileUtils.slurp("/proc/" + pid + "/cmdline");
      String cwd = fileUtils.realPath("/proc/" + pid + "/cwd");
      int nfds = fileUtils.numberOfSubFiles("/proc/" + pid + "/fd");
      int ntasks = fileUtils.numberOfSubFiles("/proc/" + pid + "/task");
      UnixPasswdParser passwdParser = new UnixPasswdParser();
      final LinuxProcessInfoParser parser = new LinuxProcessInfoParser(stat, status, cmdline,
          passwdParser.parse(), cwd, nfds, ntasks);
      return parser.parse();
    } catch (ParseException pe) {
      logger
          .warn("Caught an exception when parse process info of PID:{} from file system", pid, pe);
    } catch (IOException ioe) {
      logger.info(
          "Caught an exception when parse process info of PID:{} from file system. detail: {}", pid,
          ioe);
    }

    return null;
  }
}
