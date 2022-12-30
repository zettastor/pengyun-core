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
