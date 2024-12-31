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
