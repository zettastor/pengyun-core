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

package py.metrics;

import com.codahale.metrics.Timer.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PyTimerContextImpl implements PyTimerContext {
  private static final Logger logger = LoggerFactory.getLogger(PyTimerContextImpl.class);

  private final Context context;

  public PyTimerContextImpl(Context context) {
    if (context == null) {
      logger.error("given a null context !! {}", context, new Exception());
    }
    this.context = context;
  }

  @Override
  public long stop() {
    try {
      return context.stop();
    } catch (Throwable t) {
      logger.error("an error ?! {}", context, t);
      return -1;
    }
  }

}
