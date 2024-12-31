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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class JavaProcessBuilder {
  private static final Logger logger = Logger.getLogger(JavaProcessBuilder.class);

  private String mainClass;
  private int startingHeapSizeInMegabytes = 40;
  private int maximumHeapSizeInMegabytes = 128;
  private String workingDirectory;
  private List<String> classpathEntries = new ArrayList<String>();
  private List<String> mainClassArguments = new ArrayList<String>();
  private String jvmRuntime = null;
  private String pmIdFlag;

  public int getStartingHeapSizeInMegabytes() {
    return startingHeapSizeInMegabytes;
  }

  public JavaProcessBuilder setStartingHeapSizeInMegabytes(int startingHeapSizeInMegabytes) {
    this.startingHeapSizeInMegabytes = startingHeapSizeInMegabytes;
    return this;
  }

  public int getMaximumHeapSizeInMegabytes() {
    return maximumHeapSizeInMegabytes;
  }

  public JavaProcessBuilder setMaximumHeapSizeInMegabytes(int maximumHeapSizeInMegabytes) {
    this.maximumHeapSizeInMegabytes = maximumHeapSizeInMegabytes;
    return this;
  }

  public JavaProcessBuilder setWorkingDirectory(String workingDirectory) {
    this.workingDirectory = workingDirectory;
    return this;
  }

  public JavaProcessBuilder addClasspathEntry(String classpathEntry) {
    this.classpathEntries.add(classpathEntry);
    return this;
  }

  public JavaProcessBuilder addArgument(String argument) {
    this.mainClassArguments.add(argument);
    return this;
  }

  public JavaProcessBuilder setjvmRuntime(String jvmRuntime) {
    this.jvmRuntime = jvmRuntime;
    return this;
  }

  public JavaProcessBuilder setMainClass(String string) {
    this.mainClass = string;
    return this;
  }

  public void setPmIdFlag(String pmIdFlag) {
    this.pmIdFlag = pmIdFlag;
  }

  public Process startProcess() throws IOException {
    if (mainClass == null) {
      logger.error("please specify main class to start");
      throw new IOException();
    }

    if (jvmRuntime == null) {
      jvmRuntime =
          System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    }

    String classpath = System.getProperty("java.class.path");
    classpathEntries.add(classpath);

    List<String> argumentsList = new ArrayList<String>();
    argumentsList.add(this.jvmRuntime);
    argumentsList.add("-noverify");
    argumentsList.add("-server");
    argumentsList
        .add(MessageFormat.format("-Xms{0}M", String.valueOf(this.startingHeapSizeInMegabytes)));
    argumentsList
        .add(MessageFormat.format("-Xmx{0}M", String.valueOf(this.maximumHeapSizeInMegabytes)));
    argumentsList.add("-XX:+UseG1GC");
    argumentsList.add("-XX:MaxGCPauseMillis=100");
    argumentsList.add("-Xloggc:logs/gc.log");
    argumentsList.add("-XX:+PrintGCDetails");
    if (pmIdFlag != null) {
      argumentsList.add(pmIdFlag);
    }
    argumentsList.add("-classpath");
    argumentsList.add(getClasspath());
    argumentsList.add(this.mainClass);
    for (String arg : mainClassArguments) {
      argumentsList.add(arg);
    }

    String[] arguments = argumentsList.toArray(new String[argumentsList.size()]);
    logger.warn("The cmd of starting a java process is " + StringUtils.join(arguments, " "));
    ProcessBuilder processBuilder = new ProcessBuilder(arguments);
    processBuilder.redirectErrorStream(false);
    if (workingDirectory != null) {
      processBuilder.directory(new File(this.workingDirectory));
    }
    return processBuilder.start();
  }

  private String getClasspath() {
    StringBuilder builder = new StringBuilder();
    int count = 0;
    final int totalSize = classpathEntries.size();
    for (String classpathEntry : classpathEntries) {
      builder.append(classpathEntry);
      count++;
      if (count < totalSize) {
        builder.append(System.getProperty("path.separator"));
      }
    }
    return builder.toString();
  }

}
