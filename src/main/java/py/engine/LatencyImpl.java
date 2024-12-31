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

package py.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LatencyImpl implements Latency {
  private static final Logger logger = LoggerFactory.getLogger(LatencyImpl.class);
  private final int expectableTime;
  private long[] startEnds = new long[32];
  private Latency[] branches = new Latency[64];
  private int branchIndex = 0;
  private int index = 0;

  public LatencyImpl(int expectableTime) {
    this.expectableTime = expectableTime;
    this.startEnds[index++] = System.currentTimeMillis();
  }

  @Override
  public void mark() {
    startEnds[index] = System.currentTimeMillis();
    if (startEnds.length > index + 1) {
      index++;
    }
  }

  @Override
  public Latency createBranch() {
    Latency latency = new LatencyImpl(expectableTime);
    branches[branchIndex++] = latency;
    return latency;
  }

  @Override
  public boolean isSafe() {
    if (index == 0) {
      logger.warn("it is not called");
      return true;
    }

    if (startEnds[index - 1] - startEnds[0] < expectableTime) {
      return true;
    } else {
      return false;
    }
  }

  public String print() {
    if (index == 0 || index == 1) {
      logger.warn("not mark, index={}", index);
      return "";
    }

    StringBuilder builder = new StringBuilder();
    int maxStepIndex = 0;
    long maxStepTime = 0;
    builder.append('{');
    builder.append("start time:");
    builder.append(startEnds[0]);
    builder.append(' ');
    long lastTime = startEnds[0];
    for (int i = 1; i < index; i++) {
      long costTime = startEnds[i] - lastTime;
      lastTime = startEnds[i];
      if (costTime > maxStepTime) {
        maxStepTime = costTime;
        maxStepIndex = i;
      }

      builder.append('[');
      builder.append(i);
      builder.append(':');
      builder.append(costTime);
      builder.append(']');
      if (i != index - 1) {
        builder.append(',');
      }
    }

    builder.append(", total time:");
    builder.append(startEnds[index - 1] - startEnds[0]);
    builder.append('}');
    builder.append(", max index:");
    builder.append(maxStepIndex);
    builder.append(", cost time:");
    if (maxStepIndex == 0) {
      builder.append(startEnds[maxStepIndex]);
    } else {
      builder.append(startEnds[maxStepIndex] - startEnds[maxStepIndex - 1]);
    }
    builder.append(", ");
    if (branchIndex > 0) {
      builder.append("branch [");
      for (int i = 0; i < branchIndex; i++) {
        builder.append("branch index=" + i + ":");
        builder.append(branches[i].print());
      }

      builder.append("]");
    }

    return builder.toString();
  }
}
