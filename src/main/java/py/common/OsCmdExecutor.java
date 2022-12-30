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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsCmdExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(OsCmdExecutor.class);

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer.
   *
   * @return exit code of OS command.
   */
  public static int exec(String osCmd, ExecutorService streamConsumeExecutor,
      OsCmdStreamConsumer stdoutStreamConsumer, OsCmdStreamConsumer stderrStreamConsumer)
      throws IOException, InterruptedException {
    return exec(osCmd, null, streamConsumeExecutor, stdoutStreamConsumer, stderrStreamConsumer);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer.
   *
   * @param envp array of strings, each element of which has environment variable settings in the
   *             format name=value, or null if the subprocess should inherit the environment of the
   *             current process.
   * @return exit code of OS command.
   */
  public static int exec(String osCmd, String[] envp, ExecutorService streamConsumeExecutor,
      OsCmdStreamConsumer stdoutStreamConsumer, OsCmdStreamConsumer stderrStreamConsumer)
      throws IOException, InterruptedException {
    return exec(osCmd, envp, null, streamConsumeExecutor, stdoutStreamConsumer,
        stderrStreamConsumer);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer.
   *
   * @param streamConsumeExecutor thread pool for STDOUT consumer and STDERR consumer.
   * @param envp                  array of strings, each element of which has environment variable
   *                              settings in the format name=value, or null if the subprocess
   *                              should inherit the environment of the current process.
   * @param dir                   the working directory of the subprocess, or null if the subprocess
   *                              should inherit the working directory of the current process.
   * @return exit code of OS command.
   */
  public static int exec(String osCmd, String[] envp, File dir,
      ExecutorService streamConsumeExecutor,
      OsCmdStreamConsumer stdoutStreamConsumer, OsCmdStreamConsumer stderrStreamConsumer)
      throws IOException, InterruptedException {
    Process osCmdProcess;
    osCmdProcess = Runtime.getRuntime().exec(osCmd, envp, dir);

    Future<?> stdoutConsumerFuture;
    InputStream stdoutStream;
    stdoutStream = osCmdProcess.getInputStream();
    stdoutConsumerFuture = streamConsumeExecutor.submit(() -> {
      try {
        stdoutStreamConsumer.consume(stdoutStream);
      } catch (IOException e) {
        LOG.error("Caught an exception when executing os command [ {} ]", osCmd, e);
      }
    });

    Future<?> stderrConsumerFuture;
    InputStream stderrStream;
    stderrStream = osCmdProcess.getErrorStream();
    stderrConsumerFuture = streamConsumeExecutor.submit(() -> {
      try {
        stdoutStreamConsumer.consume(stderrStream);
      } catch (IOException e) {
        LOG.error("Caught an exception when executing os command [ {} ]", osCmd, e);
      }
    });
    osCmdProcess.waitFor();
    try {
      stdoutConsumerFuture.get();
      stderrConsumerFuture.get();
    } catch (InterruptedException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("Caught an exception", e);
      throw new IOException(e);
    }

    return osCmdProcess.exitValue();
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer.
   *
   * @return exit code of OS command.
   */
  public static int exec(String[] osCmds, ExecutorService streamConsumeExecutor,
      OsCmdStreamConsumer stdoutStreamConsumer, OsCmdStreamConsumer stderrStreamConsumer)
      throws IOException, InterruptedException {
    return exec(osCmds, null, streamConsumeExecutor, stdoutStreamConsumer, stderrStreamConsumer);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer.
   *
   * @param envp array of strings, each element of which has environment variable settings in the
   *             format name=value, or null if the subprocess should inherit the environment of the
   *             current process.
   * @return exit code of OS command.
   */
  public static int exec(String[] osCmds, String[] envp, ExecutorService streamConsumeExecutor,
      OsCmdStreamConsumer stdoutStreamConsumer, OsCmdStreamConsumer stderrStreamConsumer)
      throws IOException, InterruptedException {
    return exec(osCmds, envp, null, streamConsumeExecutor, stdoutStreamConsumer,
        stderrStreamConsumer);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer.
   *
   * @param streamConsumeExecutor thread pool for STDOUT consumer and STDERR consumer.
   * @param envp                  array of strings, each element of which has environment variable
   *                              settings in the format name=value, or null if the subprocess
   *                              should inherit the environment of the current process.
   * @param dir                   the working directory of the subprocess, or null if the subprocess
   *                              should inherit the working directory of the current process.
   * @return exit code of OS command.
   */
  public static int exec(String[] osCmds, String[] envp, File dir,
      ExecutorService streamConsumeExecutor,
      OsCmdStreamConsumer stdoutStreamConsumer, OsCmdStreamConsumer stderrStreamConsumer)
      throws IOException, InterruptedException {
    return exec(osCmds, envp, dir, null, streamConsumeExecutor, stdoutStreamConsumer,
        stderrStreamConsumer);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer.
   *
   * @param promptValue           specify string value which can be piped to process of the given
   *                              command, e.g. yes/no...
   * @param streamConsumeExecutor thread pool for STDOUT consumer and STDERR consumer.
   * @param envp                  array of strings, each element of which has environment variable
   *                              settings in the format name=value, or null if the subprocess
   *                              should inherit the environment of the current process.
   * @param dir                   the working directory of the subprocess, or null if the subprocess
   *                              should inherit the working directory of the current process.
   * @return exit code of OS command.
   */
  public static int exec(String[] osCmds, String[] envp, File dir, String promptValue,
      ExecutorService streamConsumeExecutor, OsCmdStreamConsumer stdoutStreamConsumer,
      OsCmdStreamConsumer stderrStreamConsumer) throws IOException, InterruptedException {
    Process osCmsProcess;
    OutputStream outputStream;

    osCmsProcess = Runtime.getRuntime().exec(osCmds, envp, dir);
    InputStream stdoutStream;
    InputStream stderrStream;
    stdoutStream = osCmsProcess.getInputStream();
    stderrStream = osCmsProcess.getErrorStream();

    Future<?> stdoutConsumerFuture;
    stdoutConsumerFuture = streamConsumeExecutor.submit(() -> {
      try {
        stdoutStreamConsumer.consume(stdoutStream);
      } catch (IOException e) {
        LOG.error("Caught an exception when executing os command [ {} ]", osCmds, e);
      }
    });

    Future<?> stderrConsumerFuture;
    stderrConsumerFuture = streamConsumeExecutor.submit(() -> {
      try {
        stderrStreamConsumer.consume(stderrStream);
      } catch (IOException e) {
        LOG.error("Caught an exception when executing os command [ {} ]", osCmds, e);
      }
    });

    if (promptValue != null) {
      outputStream = osCmsProcess.getOutputStream();
      try {
        outputStream.write(promptValue.getBytes());
      } finally {
        try {
          outputStream.close();
        } catch (IOException e) {
          LOG.warn("Unable to close pipe to command: {}", osCmds, e);
        }
      }
    }

    osCmsProcess.waitFor();
    try {
      stdoutConsumerFuture.get();
      stderrConsumerFuture.get();
    } catch (InterruptedException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("Caught an exception", e);
      throw new IOException(e);
    }

    return osCmsProcess.exitValue();
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer.
   *
   * <p>This method will create a fix size thread pool for consuming STDOUT and STDERR stream for
   * each invoke. For resource better management, it is prefer to use {@link
   * OsCmdExecutor#exec(String, ExecutorService, OsCmdStreamConsumer, OsCmdStreamConsumer)} rather
   * than this method.
   *
   * @return exit code of OS command
   */
  public static int exec(String osCmd, OsCmdStreamConsumer stdoutStreamConsumer,
      OsCmdStreamConsumer stderrStreamConsumer) throws IOException, InterruptedException {
    return exec(osCmd, (String[]) null, (File) null, stdoutStreamConsumer, stderrStreamConsumer);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer.
   *
   * <p>This method will create a fix size thread pool for consuming STDOUT and STDERR stream for
   * each invoke. For resource better management, it is prefer to use {@link
   * OsCmdExecutor#exec(String, ExecutorService, OsCmdStreamConsumer, OsCmdStreamConsumer)} rather
   * than this method.
   *
   * @param envp array of strings, each element of which has environment variable settings in the
   *             format name=value, or null if the subprocess should inherit the environment of the
   *             current process.
   * @param dir  the working directory of the subprocess, or null if the subprocess should inherit
   *             the working directory of the current process.
   * @return exit code of OS command
   */
  public static int exec(String osCmd, String[] envp, File dir,
      OsCmdStreamConsumer stdoutStreamConsumer,
      OsCmdStreamConsumer stderrStreamConsumer) throws IOException, InterruptedException {
    int exitCode;
    ExecutorService executorService = Executors.newFixedThreadPool(2, new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        return new Thread(r, "OS CMD Consumer");
      }
    });

    exitCode = exec(osCmd, envp, dir, executorService, stdoutStreamConsumer, stderrStreamConsumer);
    executorService.shutdown();
    if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
      LOG.warn("OS command consumers for [ {} ] havn't been finished yet!", osCmd);
    }
    return exitCode;
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer.
   *
   * <p>This method will create a fix size thread pool for consuming STDOUT and STDERR stream for
   * each invoke. For resource better management, it is prefer to use {@link
   * OsCmdExecutor#exec(String, String[], ExecutorService, OsCmdStreamConsumer,
   * OsCmdStreamConsumer)} rather than this method.
   *
   * @param envp array of strings, each element of which has environment variable settings in the
   *             format name=value, or null if the subprocess should inherit the environment of the
   *             current process.
   * @return exit code of OS command
   */
  public static int exec(String osCmd, String[] envp, OsCmdStreamConsumer stdoutStreamConsumer,
      OsCmdStreamConsumer stderrStreamConsumer) throws IOException, InterruptedException {
    return exec(osCmd, envp, (File) null, stdoutStreamConsumer, stderrStreamConsumer);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer.
   *
   * <p>This method will create a fix size thread pool for consuming STDOUT and STDERR stream for
   * each invoke. For resource better management, it is prefer to use {@link
   * OsCmdExecutor#exec(String, ExecutorService, OsCmdStreamConsumer, OsCmdStreamConsumer)} rather
   * than this method.
   *
   * @return exit code of OS command
   */
  public static int exec(String[] osCmds, OsCmdStreamConsumer stdoutStreamConsumer,
      OsCmdStreamConsumer stderrStreamConsumer) throws IOException, InterruptedException {
    return exec(osCmds, (String[]) null, (File) null, stdoutStreamConsumer, stderrStreamConsumer);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer.
   *
   * <p>This method will create a fix size thread pool for consuming STDOUT and STDERR stream for
   * each invoke. For resource better management, it is prefer to use {@link
   * OsCmdExecutor#exec(String, ExecutorService, OsCmdStreamConsumer, OsCmdStreamConsumer)} rather
   * than this method.
   *
   * @param promptValue specify string value which can be piped to process of the given command,
   *                    e.g. yes/no...
   */
  public static int exec(String[] osCmds, String promptValue,
      OsCmdStreamConsumer stdoutStreamConsumer,
      OsCmdStreamConsumer stderrStreamConsumer) throws IOException, InterruptedException {
    return exec(osCmds, promptValue, (String[]) null, (File) null, stdoutStreamConsumer,
        stderrStreamConsumer);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer.
   *
   * <p>This method will create a fix size thread pool for consuming STDOUT and STDERR stream for
   * each invoke. For resource better management, it is prefer to use {@link
   * OsCmdExecutor#exec(String, String[], ExecutorService, OsCmdStreamConsumer,
   * OsCmdStreamConsumer)} rather than this method.
   *
   * @param envp array of strings, each element of which has environment variable settings in the
   *             format name=value, or null if the subprocess should inherit the environment of the
   *             current process.
   */
  public static int exec(String[] osCmds, String[] envp, OsCmdStreamConsumer stdoutStreamConsumer,
      OsCmdStreamConsumer stderrStreamConsumer) throws IOException, InterruptedException {
    return exec(osCmds, envp, (File) null, stdoutStreamConsumer, stderrStreamConsumer);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer.
   *
   * <p>This method will create a fix size thread pool for consuming STDOUT and STDERR stream for
   * each invoke. For resource better management, it is prefer to use {@link
   * OsCmdExecutor#exec(String, ExecutorService, OsCmdStreamConsumer, OsCmdStreamConsumer)} rather
   * than this method.
   *
   * @param envp array of strings, each element of which has environment variable settings in the
   *             format name=value, or null if the subprocess should inherit the environment of the
   *             current process.
   * @param dir  the working directory of the subprocess, or null if the subprocess should inherit
   *             the working directory of the current process.
   */
  public static int exec(String[] osCmds, String[] envp, File dir,
      OsCmdStreamConsumer stdoutStreamConsumer,
      OsCmdStreamConsumer stderrStreamConsumer) throws IOException, InterruptedException {
    return exec(osCmds, null, envp, dir, stdoutStreamConsumer, stderrStreamConsumer);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer.
   *
   * <p>This method will create a fix size thread pool for consuming STDOUT and STDERR stream for
   * each invoke. For resource better management, it is prefer to use {@link
   * OsCmdExecutor#exec(String, ExecutorService, OsCmdStreamConsumer, OsCmdStreamConsumer)} rather
   * than this method.
   *
   * @param promptValue specify string value which can be piped to process of the given command,
   *                    e.g. yes/no...
   * @param envp        array of strings, each element of which has environment variable settings in
   *                    the format name=value, or null if the subprocess should inherit the
   *                    environment of the current process.
   * @param dir         the working directory of the subprocess, or null if the subprocess should
   *                    inherit the working directory of the current process.
   */
  public static int exec(String[] osCmds, String promptValue, String[] envp, File dir,
      OsCmdStreamConsumer stdoutStreamConsumer, OsCmdStreamConsumer stderrStreamConsumer)
      throws IOException, InterruptedException {
    int exitCode;
    ExecutorService executorService = Executors.newFixedThreadPool(2, new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        return new Thread(r, "OS CMD Consumer");
      }
    });

    exitCode = exec(osCmds, envp, dir, promptValue, executorService, stdoutStreamConsumer,
        stderrStreamConsumer);
    executorService.shutdown();
    if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
      LOG.warn("OS command consumers for [ {} ] havn't been finished yet!", osCmds);
    }
    return exitCode;
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer which are both instance
   * of {@link OsCmsNullConsumer}.
   *
   * <p>This method will create a fix size thread pool for consuming STDOUT and STDERR stream for
   * each invoke. For resource better management, it is prefer to use {@link
   * OsCmdExecutor#exec(String, ExecutorService, OsCmdStreamConsumer, OsCmdStreamConsumer)} rather
   * than this method.
   */
  public static int exec(String osCmd) throws IOException, InterruptedException {
    return exec(osCmd, (String[]) null, (File) null);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer which are both instance
   * of {@link OsCmsNullConsumer}.
   *
   * <p>This method will create a fix size thread pool for consuming STDOUT and STDERR stream for
   * each invoke. For resource better management, it is prefer to use {@link
   * OsCmdExecutor#exec(String, ExecutorService, OsCmdStreamConsumer, OsCmdStreamConsumer)} rather
   * than this method.
   *
   * @param envp array of strings, each element of which has environment variable settings in the
   *             format name=value, or null if the subprocess should inherit the environment of the
   *             current process.
   */
  public static int exec(String osCmd, String[] envp) throws IOException, InterruptedException {
    return exec(osCmd, envp, null);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer which are both instance
   * of {@link OsCmsNullConsumer}.
   *
   * <p>This method will create a fix size thread pool for consuming STDOUT and STDERR stream for
   * each invoke. For resource better management, it is prefer to use {@link
   * OsCmdExecutor#exec(String, ExecutorService, OsCmdStreamConsumer, OsCmdStreamConsumer)} rather
   * than this method.
   *
   * @param envp array of strings, each element of which has environment variable settings in the
   *             format name=value, or null if the subprocess should inherit the environment of the
   *             current process.
   * @param dir  the working directory of the subprocess, or null if the subprocess should inherit
   *             the working directory of the current process.
   */
  public static int exec(String osCmd, String[] envp, File dir)
      throws IOException, InterruptedException {
    OsCmsNullConsumer osCmdConsumer = new OsCmsNullConsumer();
    return exec(osCmd, envp, dir, osCmdConsumer, osCmdConsumer);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer which are both instance
   * of {@link OsCmsNullConsumer}.
   *
   * <p>This method will create a fix size thread pool for consuming STDOUT and STDERR stream for
   * each invoke. For resource better management, it is prefer to use {@link
   * OsCmdExecutor#exec(String, ExecutorService, OsCmdStreamConsumer, OsCmdStreamConsumer)} rather
   * than this method.
   */
  public static int exec(String[] osCmds) throws IOException, InterruptedException {
    return exec(osCmds, (String[]) null, (File) null);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer which are both instance
   * of {@link OsCmsNullConsumer}.
   *
   * <p>This method will create a fix size thread pool for consuming STDOUT and STDERR stream for
   * each invoke. For resource better management, it is prefer to use {@link
   * OsCmdExecutor#exec(String, ExecutorService, OsCmdStreamConsumer, OsCmdStreamConsumer)} rather
   * than this method.
   *
   * @param promptValue specify string value which can be piped to process of the given command,
   *                    e.g. yes/no...
   */
  public static int exec(String[] osCmds, String promptValue)
      throws IOException, InterruptedException {
    return exec(osCmds, promptValue, (String[]) null, (File) null);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer which are both instance
   * of {@link OsCmsNullConsumer}.
   *
   * <p>This method will create a fix size thread pool for consuming STDOUT and STDERR stream for
   * each invoke. For resource better management, it is prefer to use {@link
   * OsCmdExecutor#exec(String, ExecutorService, OsCmdStreamConsumer, OsCmdStreamConsumer)} rather
   * than this method.
   *
   * @param envp array of strings, each element of which has environment variable settings in the
   *             format name=value, or null if the subprocess should inherit the environment of the
   *             current process.
   */
  public static int exec(String[] osCmds, String[] envp) throws IOException, InterruptedException {
    return exec(osCmds, envp, null);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer which are both instance
   * of {@link OsCmsNullConsumer}.
   *
   * <p>This method will create a fix size thread pool for consuming STDOUT and STDERR stream for
   * each invoke. For resource better management, it is prefer to use {@link
   * OsCmdExecutor#exec(String, ExecutorService, OsCmdStreamConsumer, OsCmdStreamConsumer)} rather
   * than this method.
   *
   * @param envp array of strings, each element of which has environment variable settings in the
   *             format name=value, or null if the subprocess should inherit the environment of the
   *             current process.
   */
  public static int exec(String[] osCmds, String[] envp, File dir)
      throws IOException, InterruptedException {
    OsCmsNullConsumer osCmsConsumer = new OsCmsNullConsumer();
    return exec(osCmds, envp, dir, osCmsConsumer, osCmsConsumer);
  }

  /**
   * Execute the given OS command with STDOUT consumer and STDERR consumer which are both instance
   * of {@link OsCmsNullConsumer}.
   *
   * <p>This method will create a fix size thread pool for consuming STDOUT and STDERR stream for
   * each invoke. For resource better management, it is prefer to use {@link
   * OsCmdExecutor#exec(String, ExecutorService, OsCmdStreamConsumer, OsCmdStreamConsumer)} rather
   * than this method.
   *
   * @param envp array of strings, each element of which has environment variable settings in the
   *             format name=value, or null if the subprocess should inherit the environment of the
   *             current process.
   */
  public static int exec(String[] osCmds, String promptValue, String[] envp, File dir)
      throws IOException, InterruptedException {
    OsCmsNullConsumer osCmdConsumer = new OsCmsNullConsumer();
    return exec(osCmds, promptValue, envp, dir, osCmdConsumer, osCmdConsumer);
  }

  public static int execWithTimeoutMs(String osCmd, OsCmdStreamConsumer stdoutStreamConsumer,
      OsCmdStreamConsumer stderrStreamConsumer, long timeoutMs)
      throws IOException, InterruptedException {
    ExecutorService executorService = Executors.newFixedThreadPool(2, new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        return new Thread(r, "OS CMD Consumer");
      }
    });

    Process osCmdProcess;
    osCmdProcess = Runtime.getRuntime().exec(osCmd, null, null);

    InputStream stdoutStream;
    stdoutStream = osCmdProcess.getInputStream();
    Future<?> stdoutConsumerFuture;
    stdoutConsumerFuture = executorService.submit(() -> {
      try {
        stdoutStreamConsumer.consume(stdoutStream);
      } catch (IOException e) {
        LOG.error("Caught an exception when executing os command [ {} ]", osCmd, e);
      }
    });

    Future<?> stderrConsumerFuture;
    InputStream stderrStream;
    stderrStream = osCmdProcess.getErrorStream();
    stderrConsumerFuture = executorService.submit(() -> {
      try {
        stdoutStreamConsumer.consume(stderrStream);
      } catch (IOException e) {
        LOG.error("Caught an exception when executing os command [ {} ]", osCmd, e);
      }
    });

    osCmdProcess.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
    try {
      stdoutConsumerFuture.get();
      stderrConsumerFuture.get();
    } catch (InterruptedException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("Caught an exception", e);
      throw new IOException(e);
    }

    int exitCode;
    exitCode = osCmdProcess.exitValue();
    executorService.shutdown();
    if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
      LOG.warn("OS command consumers for [ {} ] havn't been finished yet!", osCmd);
    }
    return exitCode;
  }

  public static int execWithTime(String[] osCmds, OsCmdStreamConsumer stdoutStreamConsumer,
      OsCmdStreamConsumer stderrStreamConsumer, long timeoutMs)
      throws IOException, InterruptedException {
    ExecutorService executorService = Executors.newFixedThreadPool(2, new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        return new Thread(r, "OS CMD Consumer");
      }
    });

    Process osCmdProcess;

    osCmdProcess = Runtime.getRuntime().exec(osCmds, null, null);
    InputStream stdoutStream;
    stdoutStream = osCmdProcess.getInputStream();

    Future<?> stdoutConsumerFuture;
    stdoutConsumerFuture = executorService.submit(() -> {
      try {
        stdoutStreamConsumer.consume(stdoutStream);
      } catch (IOException e) {
        LOG.error("Caught an exception when executing os command [ {} ]", osCmds, e);
      }
    });

    InputStream stderrStream;
    stderrStream = osCmdProcess.getErrorStream();
    Future<?> stderrConsumerFuture;
    stderrConsumerFuture = executorService.submit(() -> {
      try {
        stderrStreamConsumer.consume(stderrStream);
      } catch (IOException e) {
        LOG.error("Caught an exception when executing os command [ {} ]", osCmds, e);
      }
    });

    if (timeoutMs > 0) {
      osCmdProcess.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
    } else {
      osCmdProcess.waitFor();
    }
    try {
      stdoutConsumerFuture.get();
      stderrConsumerFuture.get();
    } catch (InterruptedException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("Caught an exception", e);
      throw new IOException(e);
    }

    int exitCode;
    exitCode = osCmdProcess.exitValue();
    executorService.shutdown();
    if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
      LOG.warn("OS command consumers for [ {} ] havn't been finished yet!", osCmds);
    }
    return exitCode;
  }

  public static interface OsCmdStreamConsumer {
    public void consume(InputStream stream) throws IOException;
  }

  /**
   * The instance of this class just consume all output in kind STDOUT and STDERR.
   */
  public static class OsCmsNullConsumer implements OsCmdStreamConsumer {
    @Override
    public void consume(InputStream stream) throws IOException {
      while (stream.read() >= 0) {
      }
    }
  }

  /**
   * The instance of this class logging each line of output in kind STDOUT and STDERR of some OS
   * command after running.
   */
  public static class OsCmdOutputLogger implements OsCmdStreamConsumer {
    private final Logger logger;

    private final String osCmd;

    private boolean errorStream = false;

    public OsCmdOutputLogger(String osCmd) {
      this(LOG, osCmd);
    }

    public OsCmdOutputLogger(Logger logger, String osCmd) {
      this.logger = logger;
      this.osCmd = osCmd;
    }

    public boolean isErrorStream() {
      return errorStream;
    }

    public void setErrorStream(boolean errorStream) {
      this.errorStream = errorStream;
    }

    @Override
    public void consume(InputStream stream) throws IOException {
      String line = null;
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

      while ((line = reader.readLine()) != null) {
        if (isErrorStream()) {
          logger.warn("An output line for command [ {} ]: {}", osCmd, line);
        } else {
          logger.info("An output line for command [ {} ]: {}", osCmd, line);
        }
      }
    }
  }
}
