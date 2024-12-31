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

public class ExecutionOptionsReader {
  private int maxNumWorkers;
  private int numWorkers;
  private Integer fixedRate;
  private Integer fixedDelay;

  public ExecutionOptionsReader(int maxNumWorkers,
      int numWorkers,
      Integer fixedRate,
      Integer fixedDelay) {
    this.maxNumWorkers = maxNumWorkers;
    this.numWorkers = numWorkers;
    this.fixedRate = fixedRate;
    this.fixedDelay = fixedDelay;
  }

  public ExecutionOptions read() throws InvalidExecutionOptionsException {
    return new ExecutionOptions(maxNumWorkers, numWorkers, fixedRate, fixedDelay);
  }

  public void setMaxNumWorkers(int maxNumWorkers) {
    this.maxNumWorkers = maxNumWorkers;
  }

  public void setNumWorkers(int numWorkers) {
    this.numWorkers = numWorkers;
  }

  public void setFixedRate(Integer fixedRate) {
    this.fixedRate = fixedRate;
  }

  public void setFixedDelay(Integer fixedDelay) {
    this.fixedDelay = fixedDelay;
  }
}
