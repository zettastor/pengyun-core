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

package py.token.controller;

import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.Validate;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.periodic.UnableToStartException;
import py.periodic.Worker;
import py.periodic.WorkerFactory;
import py.periodic.impl.ExecutionOptionsReader;
import py.periodic.impl.PeriodicWorkExecutorImpl;

public class TokenControllerCenter {
  private static final Logger logger = LoggerFactory.getLogger(TokenControllerCenter.class);

  private static final TokenControllerCenter center = new TokenControllerCenter();
  private static final int FIXED_DELAY_MS = 1000;
  private final Map<Long, TokenController> mapIdToIoController;
  private PeriodicWorkExecutorImpl executor;
  private int maxWorkerCount;
  private int workerCount;

  private TokenControllerCenter() {
    this.mapIdToIoController = new ConcurrentHashMap<Long, TokenController>();
    this.maxWorkerCount = 1;
    this.workerCount = 1;
  }

  public static TokenControllerCenter getInstance() {
    return center;
  }

  public void setMaxWorkerCount(int maxWorkerCount) {
    this.maxWorkerCount = maxWorkerCount;
  }

  public void setWorkerCount(int workerCount) {
    this.workerCount = workerCount;
  }

  public void start() throws UnableToStartException {
    Validate.isTrue(executor == null);
    ExecutionOptionsReader reader = new ExecutionOptionsReader(maxWorkerCount, workerCount,
        FIXED_DELAY_MS, null);
    executor = new PeriodicWorkExecutorImpl(reader, new ControlIoWorkerFactory(), "io-controller");
    executor.start();

  }

  public void register(TokenController controller) {
    logger.info("register controller: {}", controller);
    mapIdToIoController.put(controller.getId(), controller);
  }

  public void deregister(TokenController controller) {
    logger.info("unregister controller: {}", controller);
    mapIdToIoController.remove(controller.getId());
  }

  public void stop() {
    if (executor != null) {
      executor.stop();
      executor = null;
    }
  }

  public PeriodicWorkExecutorImpl getExecutor() {
    return executor;
  }

  private class ControlIoWorkerFactory implements WorkerFactory {
    @Override
    public Worker createWorker() {
      return new Worker() {
        @Override
        public void doWork() throws Exception {
          for (Entry<Long, TokenController> entry : mapIdToIoController.entrySet()) {
            TokenController controller = entry.getValue();
            try {
              controller.reset();
              logger.trace("reset controller: {}", controller);
            } catch (Throwable t) {
              logger.error("can not reset controller: {}", controller);
            }
          }
        }
      };
    }

  }
}
