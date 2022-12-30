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

package py.consumer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.NamedThreadFactory;

public class SingleThreadRunnableConsumerService<T extends Runnable> implements ConsumerService<T> {
  private static final Logger logger = LoggerFactory
      .getLogger(SingleThreadRunnableConsumerService.class);
  private final ExecutorService executor;

  public SingleThreadRunnableConsumerService(String name) {
    executor = Executors.newSingleThreadExecutor(new NamedThreadFactory(name));
  }

  public SingleThreadRunnableConsumerService(String name, BlockingQueue<Runnable> workQueue) {
    executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, workQueue,
        new NamedThreadFactory(name));
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
    executor.shutdown();
    try {
      executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
    } catch (InterruptedException ignore) {
      logger.error("caught exception", ignore);
    }
  }

  @Override
  public boolean submit(T element) {
    executor.execute(element);
    return true;
  }
}
