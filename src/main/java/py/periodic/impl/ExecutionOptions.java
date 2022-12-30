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

package py.periodic.impl;

public final class ExecutionOptions {
  private final int maxNumWorkers;

  private final int numWorkers;

  private final Integer fixedRate;

  private final Integer fixedDelay;

  public ExecutionOptions(
      int maxNumWorkers,
      int numWorkers,
      Integer fixedRate,
      Integer fixedDelay)
      throws InvalidExecutionOptionsException {
    if (maxNumWorkers <= 0 || numWorkers <= 0 || maxNumWorkers < numWorkers) {
      throw new InvalidExecutionOptionsException(
          "maxNumWorkers and numWorkers must be positive and "
              + "maxNumWorkers may not be less than numWorkers");
    }

    if (!((fixedRate != null && fixedDelay == null)
        || (fixedRate == null && fixedDelay != null))) {
      throw new InvalidExecutionOptionsException("Either fixedRate or fixedDelay must be set");
    }

    if (fixedRate != null && fixedRate < 0) {
      throw new InvalidExecutionOptionsException("fixedRate must be postive");
    }

    if (fixedDelay != null && fixedDelay < 0) {
      throw new InvalidExecutionOptionsException("fixedDelay must be postive");
    }

    this.maxNumWorkers = maxNumWorkers;
    this.numWorkers = numWorkers;
    this.fixedRate = fixedRate;
    this.fixedDelay = fixedDelay;
  }

  public int getMaxNumWorkers() {
    return maxNumWorkers;
  }

  public int getNumWorkers() {
    return numWorkers;
  }

  public Integer getFixedRate() {
    return fixedRate;
  }

  public Integer getFixedDelay() {
    return fixedDelay;
  }
}
