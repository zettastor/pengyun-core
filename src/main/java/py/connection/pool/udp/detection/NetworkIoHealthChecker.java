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

package py.connection.pool.udp.detection;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.NamedThreadFactory;

public enum NetworkIoHealthChecker {
  INSTANCE;

  static final int INTERVAL_MS = 25;
  static final int TIMEOUT_MS = 600 * 1000;
  private static final Logger logger = LoggerFactory.getLogger(NetworkIoHealthChecker.class);
  private final AtomicBoolean started = new AtomicBoolean(false);
  private final AtomicReference<Function<String, Boolean>> resultForUnitTests
      = new AtomicReference<>(null);
  private Map<String, IpTask> mapEndPointToTasks;
  private Map<InetSocketAddress, String> mapAddressToIp;
  private UdpDetector udpDetector;
  private ScheduledExecutorService executorService;

  public synchronized void start(UdpDetector udpDetector) {
    if (!started.get()) {
      this.udpDetector = udpDetector;
      this.mapEndPointToTasks = new ConcurrentHashMap<>();
      this.mapAddressToIp = new ConcurrentHashMap<>();
      this.executorService = Executors
          .newSingleThreadScheduledExecutor(
              new NamedThreadFactory("network-io-health-checker", true));
      started.set(true);
    }
  }

  public void setResultForTestsOnly(Function<String, Boolean> reachable) {
    resultForUnitTests.set(reachable);
  }

  public Future<?> markReachableAndKeepChecking(InetSocketAddress address) {
    if (started.get()) {
      return markReachableAndKeepChecking(
          mapAddressToIp.computeIfAbsent(address, a -> a.getAddress().getHostAddress()));
    } else {
      return CompletableFuture.completedFuture(null);
    }
  }

  public Future<?> markReachableAndKeepChecking(String ip) {
    return refreshTask(ip, true);
  }

  public void keepChecking(String ip) {
    refreshTask(ip, false);
  }

  private Future<?> refreshTask(String ip, boolean markReachable) {
    if (started.get()) {
      return executorService.submit(() -> {
        IpTask task = mapEndPointToTasks.computeIfAbsent(ip, i -> {
          IpTask ipTask = new IpTask(ip);
          executorService.execute(ipTask);
          return ipTask;
        });

        if (markReachable) {
          task.markReachable();
        }

        task.active();
      });

    }
    return CompletableFuture.completedFuture(null);
  }

  public boolean blockingCheck(String ip) {
    if (resultForUnitTests.get() != null) {
      return resultForUnitTests.get().apply(ip);
    }

    if (!started.get()) {
      return true;
    } else {
      try {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        executorService.execute(() -> {
          IpTask task = mapEndPointToTasks.computeIfAbsent(ip, IpTask::new);
          if (task.reachable.get()) {
            task.detect().thenAccept(future::complete);
          } else {
            future.complete(false);
          }
        });
        return future.get();
      } catch (InterruptedException | ExecutionException e) {
        logger.error("exception caught", e);
        return true;
      }
    }

  }

  public boolean isUnreachable(String ip) {
    if (resultForUnitTests.get() != null) {
      return !resultForUnitTests.get().apply(ip);
    }

    if (!started.get()) {
      return false;
    } else {
      IpTask task = mapEndPointToTasks.get(ip);
      if (task == null) {
        return false;
      } else {
        return !task.reachable.get();
      }
    }
  }

  private class IpTask implements Runnable {
    private final String ip;
    private AtomicBoolean reachable = new AtomicBoolean(true);
    private volatile long lastReachableTime;
    private volatile long lastActiveTime;

    private IpTask(String ip) {
      this.ip = ip;
      this.lastReachableTime = 0;
      active();
    }

    void markReachable() {
      reachable.set(true);
      lastReachableTime = System.currentTimeMillis();
    }

    void active() {
      this.lastActiveTime = System.currentTimeMillis();
    }

    private CompletableFuture<Boolean> detect() {
      return udpDetector.detect(ip).thenApply(result -> {
        if (reachable.get() != result) {
          logger.warn("host [{}] reachable changed from {} to {}", ip,
              reachable.get(), result);
        }

        reachable.set(result);

        int delay;
        if (result) {
          lastReachableTime = System.currentTimeMillis();
          delay = INTERVAL_MS;
        } else {
          delay = 0;
        }

        executorService.schedule(this, delay, TimeUnit.MILLISECONDS);

        return result;

      });
    }

    @Override
    public void run() {
      if (System.currentTimeMillis() - lastReachableTime < INTERVAL_MS) {
        executorService.schedule(this, INTERVAL_MS, TimeUnit.MILLISECONDS);
        return;
      }

      if (System.currentTimeMillis() - lastActiveTime > TIMEOUT_MS) {
        logger.warn("exit an idle checking task {}", ip);
        mapEndPointToTasks.remove(ip);
        return;
      }

      detect();
    }

  }

}
