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

package py.engine.disruptor;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PyDisruptor {
  private static final Logger logger = LoggerFactory.getLogger(PyDisruptor.class);
  private static final int ringBufferSize = 1024 * 1024;

  private final Disruptor<PyEvent> disruptor;
  private int numProcessor;

  public PyDisruptor(ProducerType producerType, int numProcessor, WaitStrategy waitStrategy) {
    PyEventFactory pyEventFactory = new PyEventFactory();
    this.disruptor = new Disruptor(pyEventFactory, ringBufferSize, Executors.defaultThreadFactory(),
        producerType,
        waitStrategy);
    this.numProcessor = numProcessor;
  }

  public void start() {
    this.disruptor.start();
  }

  public void shutdown() {
    this.disruptor.shutdown();
  }

  public void initEventHandlers(EventHandler<PyEvent>[] eventHandlers) {
    if (eventHandlers.length != numProcessor) {
      logger.error("event handler number:{} not equals to number:{} target before, check it!",
          eventHandlers.length, numProcessor);
      Validate.isTrue(false);
    }
    this.disruptor.handleEventsWith(eventHandlers);
  }

  public void initSingleEventHandler(EventHandler<PyEvent> eventHandler) {
    if (numProcessor != 1) {
      logger.error("event handler number:1 not equals to number:{} target before, check it!",
          numProcessor);
      Validate.isTrue(false);
    }
    this.disruptor.handleEventsWith(eventHandler);
  }

  public Disruptor<PyEvent> getDisruptor() {
    return disruptor;
  }
}
