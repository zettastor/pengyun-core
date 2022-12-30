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

package py.common;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.OsCmdExecutor.OsCmdStreamConsumer;

public class ProcessRelated {
  private static final Logger logger = LoggerFactory.getLogger(ProcessRelated.class);
  private static String processString;

  public static List<String> getProResult(String cmd) {
    return getProResult(cmd, 0);
  }

  public static List<String> getProResult(String cmd, long timeoutMs) {
    List<String> cmdResult = new ArrayList<>();
    Process process = null;
    InputStreamReader inputStreamReader = null;
    LineNumberReader br = null;
    try {
      // this work for pipe, since pipe didn't have return without /bin/sh -c
      String[] cmdArr = {"/bin/sh", "-c", cmd};
      process = Runtime.getRuntime().exec(cmdArr);
      if (timeoutMs <= 0) {
        process.waitFor();
      } else {
        boolean isTimeout = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        if (!isTimeout) {
          logger.warn("cmd:{} run timeout in {}ms", cmd, timeoutMs);
          throw new Exception("cmd run timeout");
        }
      }

      inputStreamReader = new InputStreamReader(process.getInputStream());
      br = new LineNumberReader(inputStreamReader);

      String line;
      while ((line = br.readLine()) != null) {
        if (!line.equals("")) {
          cmdResult.add(line);
        }
      }
    } catch (Exception e) {
      logger.warn("exec cmd:{} cause exception ", cmd, e);
    } finally {
      try {
        if (br != null) {
          br.close();
        }
      } catch (IOException e) {
        logger.error("close buffer reader handle failed!", e);
      }
      try {
        if (inputStreamReader != null) {
          inputStreamReader.close();
        }
      } catch (IOException e) {
        logger.error("close inputStreamReader handle failed!", e);
      }

      if (process != null) {
        process.destroy();
      }
    }
    logger.debug("execute {} ProcessRelated got {} result", cmd, cmdResult);
    return cmdResult;
  }

  public static String waitForProcessExit(Process process, int timeout, TimeUnit timeUnit)
      throws InterruptedException {
    if (process == null) {
      return null;
    }

    /* try to readEventData from input stream or error stream in case the thread is blocked;
     * try readEventData from error stream
     * */
    Thread errorThread = new Thread("error") {
      public void run() {
        InputStream is1 = process.getErrorStream();
        BufferedReader br1 = new BufferedReader(new InputStreamReader(is1));
        try {
          String line1;
          while ((line1 = br1.readLine()) != null) {
            logger.warn("error line={}", line1);
          }
        } catch (IOException e) {
          logger.warn("readEventData from error stream", e);
        } finally {
          try {
            is1.close();
          } catch (IOException e) {
            logger.error("caught a thread io exception", e);
          }
        }
        logger.trace("exit the error thread={}", Thread.currentThread().getId());
      }
    };
    errorThread.start();

    Thread readThread = new Thread("input") {
      public void run() {
        InputStream is2 = process.getInputStream();

        processString = convertStream2Json(is2);

        logger.info("process json string return is {}", processString);
        logger.trace("exit the readEventData thread={}", Thread.currentThread().getId());
      }
    };
    readThread.start();

    long startTime = System.currentTimeMillis();
    boolean result = process.waitFor(timeout, timeUnit);
    errorThread.join(1000);
    readThread.join(1000);
    logger.info("cost time={}, result={}", System.currentTimeMillis() - startTime, result);
    try {
      process.getOutputStream().close();
    } catch (IOException e) {
      logger.warn("process close outputstream cause an error {}", e);
    } finally {
      process.destroy();
    }
    return processString;
  }

  private static String convertStream2Json(InputStream inputStream) {
    String jsonStr = "";
    // ByteArrayOutputStream equals to the memory sendEventData stream
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int len;
    // Transfers the input stream to the memory sendEventData stream
    try {
      while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
        out.write(buffer, 0, len);
      }
      // Converts the memory stream to a string

      jsonStr = new String(out.toByteArray());

      logger.info("convert string to json {}", jsonStr);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return jsonStr;
  }

  /*
   * exec scripts which contains cmd need input y to run
   */
  public static CmdResult executeOsCommand(String cmd) {
    String[] command = {"/bin/sh", "-c", cmd};
    return executeOsCommand(command);
  }

  public static CmdResult executeOsCommand(String[] command) {
    long id = RequestIdBuilder.get();
    logger.info("[{}]start exec cmd:{}", id, command);
    CmdResult cmdResult = new CmdResult();
    OsCmdStreamConsumer consumer = new OsCmdStreamConsumer() {
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
    OsCmdStreamConsumer errorConsumer = new OsCmdStreamConsumer() {
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
      int ret = OsCmdExecutor.exec(command, consumer, errorConsumer);
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

  public static CmdResult executeOsCommand(String cmd, CmdResult cmdResult,
      OsCmdStreamConsumer normalConsumer,
      OsCmdStreamConsumer errorConsumer)
      throws Exception {
    String[] command = {"/bin/sh", "-c", cmd};
    return executeOsCommand(command, cmdResult, normalConsumer, errorConsumer);
  }

  public static CmdResult executeOsCommand(String[] command, CmdResult cmdResult,
      OsCmdStreamConsumer normalConsumer,
      OsCmdStreamConsumer errorConsumer)
      throws Exception {
    long id = RequestIdBuilder.get();
    logger.info("[{}]start exec cmd:{}", id, command);
    if (cmdResult == null) {
      logger.error("[{}]argument is illegal", id);
      throw new Exception("argument is illegal");
    }

    if (normalConsumer == null) {
      normalConsumer = new OsCmdStreamConsumer() {
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
    }

    if (errorConsumer == null) {
      errorConsumer = new OsCmdStreamConsumer() {
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
    }

    try {
      int ret = OsCmdExecutor.exec(command, normalConsumer, errorConsumer);
      cmdResult.setRetNo(ret);
      if (0 != ret) {
        logger.warn("[{}]End exec cmd:{}, ret:{}", id, command, cmdResult);
        return cmdResult;
      }

      logger.info("[{}]End exec cmd:{}! ret:{}", id, command, cmdResult);
    } catch (Exception e) {
      cmdResult.setRetNo(-1);
      cmdResult.appendErrMsg("Caught an exception when execute command ");
      logger.error("[{}]End exec cmd:{}! set ret:{}. ", id, command, cmdResult, e);
    }

    return cmdResult;
  }
}
