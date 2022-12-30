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

package py.netty.core.twothreads;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.NotImplementedException;
import py.engine.Task;

public abstract class AbstractNetworkTask implements Task {
  @Override
  public void destroy() {
    throw new NotImplementedException("");
  }

  @Override
  public void cancel() {
    throw new NotImplementedException("");
  }

  @Override
  public boolean isCancel() {
    throw new NotImplementedException("");
  }

  @Override
  public int getToken() {
    throw new NotImplementedException("");
  }

  @Override
  public void setToken(int token) {
    throw new NotImplementedException("");
  }

  @Override
  public long getDelay(TimeUnit unit) {
    throw new NotImplementedException("");
  }

  @Override
  public int compareTo(Delayed o) {
    throw new NotImplementedException("");
  }
}
