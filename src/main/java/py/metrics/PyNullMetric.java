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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class PyNullMetric implements PyMetric {
  public static final PyNullMetric defaultNullMetric = new PyNullMetric();

  @Override
  public void incCounter() {
  }

  @Override
  public void incCounter(long n) {
  }

  @Override
  public void decCounter() {
  }

  @Override
  public void decCounter(long n) {
  }

  @Override
  public void updateHistogram(int value) {
  }

  @Override
  public void updateHistogram(long value) {
  }

  @Override
  public void mark() {
  }

  @Override
  public void mark(long n) {
  }

  @Override
  public PyTimerContext time() {
    return PyNullTimerContext.defaultNullTimerContext;
  }

  @Override
  public <T> T time(Callable<T> event) throws Exception {
    throw new RuntimeException("This method is not implemented");
  }

  @Override
  public void update(long duration, TimeUnit unit) {
  }

  @Override
  public long getCount() {
    return 0;
  }
}
