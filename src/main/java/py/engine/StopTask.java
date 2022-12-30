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

public class StopTask extends DelayedTask {
  private static final Logger logger = LoggerFactory.getLogger(StopTask.class);

  public StopTask() {
    super(0);
  }

  @Override
  public void doWork() {
  }

  @Override
  public void destroy() {
  }

  @Override
  public void cancel() {
  }

  @Override
  public boolean isCancel() {
    return true;
  }

  @Override
  public int getToken() {
    return 1;
  }

  @Override
  public void setToken(int expectedToken) {
  }

  @Override
  public Result work() {
    logger.warn("this is a stop task");
    return null;
  }
}
