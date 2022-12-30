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
