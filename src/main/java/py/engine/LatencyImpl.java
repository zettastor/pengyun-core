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
