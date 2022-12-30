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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
  private AtomicInteger no = new AtomicInteger(1);
  private String threadName;
  private boolean isDaemon;

  public NamedThreadFactory(String name) {
    this(name, false);
  }

  public NamedThreadFactory(String name, boolean daemon) {
    this.threadName = name;
    this.isDaemon = daemon;
  }

  public String getThreadName() {
    return threadName;
  }

  public void setThreadName(String threadName) {
    this.threadName = threadName;
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread t = new Thread(r, threadName + no.getAndIncrement());
    t.setDaemon(isDaemon);
    return t;
  }
}
