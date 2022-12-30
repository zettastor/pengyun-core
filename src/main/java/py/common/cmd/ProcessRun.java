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

package py.common.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.CmdResult;
import py.common.OsCmdExecutor;
import py.common.RequestIdBuilder;

public class ProcessRun {
  private static final Logger logger = LoggerFactory.getLogger(ProcessRun.class);

  public static CmdResult exec(String cmd) {
    return exec(cmd, 0);
  }

  public static CmdResult exec(String[] cmd) {
    return exec(cmd, 0);
  }

  public static CmdResult exec(String cmd, long timeoutMs) {
    String[] cmdArr = {
        "/bin/sh",
        "-c",
        cmd
    };

    return exec(cmdArr, timeoutMs);
  }

  public static CmdResult exec(String[] cmdArr, long timeoutMs) {
    long id = RequestIdBuilder.get();
    logger.info("[{}]start exec cmd:{}", id, cmdArr);
    CmdResult cmdResult = new CmdResult();
    Process process = null;
    InputStreamReader inputStreamReader = null;
    LineNumberReader br = null;
    try {
      process = Runtime.getRuntime().exec(cmdArr);
      if (timeoutMs <= 0) {
        process.waitFor();
      } else {
        boolean isTimeout = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        if (!isTimeout) {
          logger.warn("[{}]cmd:{} run timeout in {}ms", id, cmdArr, timeoutMs);
          throw new Exception("cmd run timeout");
        }
      }

      inputStreamReader = new InputStreamReader(process.getInputStream());
      br = new LineNumberReader(inputStreamReader);

      String line;
      while ((line = br.readLine()) != null) {
        if (!line.equals("")) {
          cmdResult.appendRetMsg(line);
        }
      }

      BufferedReader errorReader = new BufferedReader(
          new InputStreamReader(process.getErrorStream()));
      while ((line = errorReader.readLine()) != null) {
        cmdResult.appendErrMsg(line);
      }
    } catch (Exception e) {
      logger.warn("[{}]exec cmd:{} cause exception ", id, cmdArr, e);
    } finally {
      try {
        if (br != null) {
          br.close();
        }
      } catch (IOException e) {
        logger.error("[{}]close buffer reader handle failed!", id, e);
      }
      try {
        if (inputStreamReader != null) {
          inputStreamReader.close();
        }
      } catch (IOException e) {
        logger.error("[{}]close inputStreamReader handle failed!", id, e);
      }

      if (process != null) {
        process.destroy();
      }
      logger.info("[{}]End exec cmd:{} got result:{}.", id, cmdArr, cmdResult);
    }
    return cmdResult;
  }

  /**
   * exec scripts which contains cmd need input y to run.
   */
  public static CmdResult executeOsCommand(String cmd, long timeout) {
    String[] command = {"/bin/sh", "-c", cmd};
    return executeOsCommand(command, timeout);
  }

  public static CmdResult executeOsCommand(String[] command, long timeout) {
    long id = RequestIdBuilder.get();
    logger.info("[{}]start exec cmd:{}", id, command);
    final CmdResult cmdResult = new CmdResult();
    OsCmdExecutor.OsCmdStreamConsumer consumer = new OsCmdExecutor.OsCmdStreamConsumer() {
      @Override
      public void consume(InputStream inputStream) throws IOException {
        String line = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder normMessageBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
          cmdResult.appendRetMsg(line);
        }
       
      }
    };
    OsCmdExecutor.OsCmdStreamConsumer errorConsumer = new OsCmdExecutor.OsCmdStreamConsumer() {
      @Override
      public void consume(InputStream inputStream) throws IOException {
        String line = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder errorMessageBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
          cmdResult.appendErrMsg(line);
        }
       
      }
    };
    try {
      int ret = OsCmdExecutor.execWithTime(command, consumer, errorConsumer, timeout);
      cmdResult.setRetNo(ret);
      if (0 != ret) {
        logger.warn("[{}]End exec cmd:{}, ret:{}", id, command, cmdResult);
        return cmdResult;
      }

      logger.info("[{}]End exec cmd:{}! ret:{}", id, command, cmdResult);
    } catch (Exception e) {
      cmdResult.setRetNo(-1);
      cmdResult.appendErrMsg("Caught an exception when execute command ");
      logger.error("[{}]End exec cmd:{} failed! set ret:{}. ", id, command, cmdResult, e);
    }

    return cmdResult;
  }
}
