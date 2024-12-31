/**
* Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
