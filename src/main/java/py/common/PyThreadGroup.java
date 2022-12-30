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

import java.util.ArrayList;
import java.util.Collection;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * A threadGroup used for the threads created for pengyun concurrency applications.
 */
public class PyThreadGroup extends ThreadGroup {
  private static final Logger log = Logger.getLogger(PyThreadGroup.class);

  private final Collection<Thread> threads = new ArrayList<Thread>();

  public PyThreadGroup(String name) {
    super(name);
  }

  public void startDaemonThread(ThreadGroup threadGroup, Runnable runnable, String name) {
    Thread thread = new Thread(threadGroup, runnable, name);
    thread.setDaemon(true);
    thread.start();
    threads.add(thread);
    log.info("started thread:" + name);
  }

  public void startDaemonThread(Runnable runnable, String name) {
    startDaemonThread(this, runnable, name);
  }

  public void joinThreads() throws InterruptedException {
    for (Thread thread : threads) {
      thread.join();
    }
  }

  @Override
  public void uncaughtException(Thread t, Throwable e) {
    try {
      super.uncaughtException(t, e);

      // maybe we can get some free memory
      System.gc();

      // try to get a message into the application log
      log.fatal("thread:" + t.getName() + "threw an uncaught ecxeption", e);
      LogManager.shutdown();
    } finally {
      Runtime.getRuntime().halt(-1);
    }
  }

  public void interuptAllStarted() {
    for (Thread thread : threads) {
      thread.interrupt();
    }
  }

  public boolean areAnyThreadsAlive() {
    for (Thread thread : threads) {
      if (thread.isAlive()) {
        return true;
      }
    }
    return false;
  }

  public void joinAllStarted(long timeout) {
    for (Thread thread : threads) {
      try {
        thread.join(timeout);
      } catch (InterruptedException ie) {
        log.warn("interrupted while waiting to join thread:" + thread.getName(), ie);
      }
    }
  }

  public void joinAllStarted() {
    joinAllStarted(Integer.MAX_VALUE);
  }
}
