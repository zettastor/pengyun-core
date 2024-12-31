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

package py.common.cache;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class CachedProducerImpl<T> implements CachedProducer<T> {
  private final BlockingQueue<T> cachedProducts = new LinkedBlockingQueue<>();
  private final AtomicLong cachePoolSize = new AtomicLong(0);
  private final ExecutorService producerExecutor;
  private final int cachedProductsLowerThreshold;
  private final int cachedProductsUpperThreshold;

  private final Callable<T> producer;

  public CachedProducerImpl(ExecutorService producerExecutor, int cachedProductsLowerThreshold,
      int cachedProductsUpperThreshold, Callable<T> producer) {
    this.producerExecutor = producerExecutor;
    this.cachedProductsLowerThreshold = cachedProductsLowerThreshold;
    this.cachedProductsUpperThreshold = cachedProductsUpperThreshold;
    this.producer = producer;
  }

  @Override
  public T poll() throws Exception {
    T address = cachedProducts.poll();
    long count;

    if (address == null) {
      address = producer.call();
      count = 0;
    } else {
      count = cachePoolSize.decrementAndGet();
    }

    if (count == cachedProductsLowerThreshold || count == 0) {
      producerExecutor.execute(() -> {
        for (int i = 0; i < cachedProductsUpperThreshold; i++) {
          try {
            cachedProducts.offer(producer.call());
            if (cachePoolSize.incrementAndGet() >= cachedProductsUpperThreshold) {
              break;
            }
          } catch (Exception e) {
            break;
          }
        }
      });
    }

    return address;
  }

}
