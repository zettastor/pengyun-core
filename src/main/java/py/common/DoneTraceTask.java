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

import com.lmax.disruptor.dsl.Disruptor;
import py.engine.DelayedTask;
import py.engine.Result;

public class DoneTraceTask extends DelayedTask {
  private Disruptor<LoggerEvent> disruptor;
  private LoggerEvent loggerEvent;

  public DoneTraceTask(int delayMs, Disruptor<LoggerEvent> disruptor, LoggerEvent loggerEvent) {
    super(delayMs);
    this.disruptor = disruptor;
    this.loggerEvent = loggerEvent;
  }

  @Override
  public Result work() {
    LoggerTracer.publishForDoneTrace(disruptor, loggerEvent);
    return null;
  }
}
