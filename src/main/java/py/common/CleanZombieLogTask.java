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

import py.engine.DelayedTask;
import py.engine.Result;

public class CleanZombieLogTask extends DelayedTask {
  private final Long traceLogId;

  public CleanZombieLogTask(int delayMs, long traceLogId) {
    super(delayMs);
    this.traceLogId = traceLogId;
  }

  @Override
  public Result work() {
    LoggerTracer.getInstance().doneTrace(this.traceLogId, TraceAction.CleanTraceLog, 0);
    // for now do not care about the result of done trace
    return null;
  }
}
