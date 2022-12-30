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
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public interface PyMetric {
  public void incCounter();

  public void incCounter(long n);

  public void decCounter();

  public void decCounter(long n);

  public void updateHistogram(int value);

  public void updateHistogram(long value);

  public void mark();

  public void mark(long n);

  public long getCount();

  public PyTimerContext time();

  public <T> T time(Callable<T> event) throws Exception;

  public void update(long duration, TimeUnit unit);
}
