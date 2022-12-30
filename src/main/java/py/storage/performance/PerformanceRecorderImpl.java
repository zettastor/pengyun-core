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

package py.storage.performance;

import static java.lang.Math.floor;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.UniformSnapshot;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.reservoir.Reservoir;
import py.common.reservoir.SlidingTimeWindowReservoir;
import py.common.struct.Pair;

public class PerformanceRecorderImpl implements PerformanceRecorder {
  private static final Logger logger = LoggerFactory.getLogger(PerformanceRecorderImpl.class);

  private static final int LEAST_SAMPLE_COUNT = 500;
  private static final double IGNORE_RATIO = 0.1;

  private final Reservoir<Long> latencyReservoir;

  private final Reservoir<Long> offsetReservoir;

  private final Clock clock = Clock.defaultClock();

  private final int pageSize;

  private final AtomicInteger pendingCount;
  private final AtomicLong lastDoneTime = new AtomicLong(0);

  public PerformanceRecorderImpl(int windowSeconds, int pageSize) {
    this.latencyReservoir = new SlidingTimeWindowReservoir(windowSeconds, TimeUnit.SECONDS);
    this.offsetReservoir = new SlidingTimeWindowReservoir(windowSeconds, TimeUnit.SECONDS);
    this.pageSize = pageSize;
    this.pendingCount = new AtomicInteger(0);
    logger.warn("initializing performance recorder window seconds : {}, page size : {}",
        windowSeconds, pageSize);
  }

  @Override
  public Pair<Long, Long> getVal(double quantile, double ignorRatio) {
    Long[] offsets = offsetReservoir.getSnapshot();
    Long[] latencies = latencyReservoir.getSnapshot();
    return internalCheck(offsets, latencies, quantile, ignorRatio);
  }

  private Pair<Long, Long> internalCheck(Long[] offsets, Long[] latencies, double quantile,
      double ignoreRatio) {
    int length = Math.min(offsets.length, latencies.length);
    if (length < LEAST_SAMPLE_COUNT) {
      logger.info("not enough sample {}, expected at least {}", length, LEAST_SAMPLE_COUNT);
      return new Pair<>(0L, 0L);
    }

    LinkedList<Pair<Long, Long>> mergedRanges = new LinkedList<>();

    Pair<Long, Long> range = null;
    for (int i = 0; i < length; i++) {
      long offset = offsets[i];
      if (range == null) {
        range = new Pair<>(offset, offset + pageSize);
        continue;
      }

      if (range.getSecond() == offset) {
        range.setSecond(offset + pageSize);
        continue;
      }

      mergedRanges.addLast(range);
      range = new Pair<>(offset, offset + pageSize);
    }

    int randomCount = mergedRanges.size();
    int sequentialCount = length - randomCount;

    double sequentialRatio = (double) sequentialCount / (double) length;
    double randomRatio = 1 - sequentialRatio;

    logger.info("sequential ratio {} latencies length:{}", sequentialRatio, latencies.length);

    Arrays.sort(latencies);

    double sequentialSample =
        sequentialRatio < ignoreRatio ? 0 : getValue(sequentialRatio * quantile, latencies);
    double randomSample =
        randomRatio < ignoreRatio ? 0
            : getValue(sequentialRatio + randomRatio * quantile, latencies);

    return new Pair<>((long) randomSample, (long) sequentialSample);
  }

  private double getValue(double quantile, Long[] values) {
    if (quantile < 0.0 || quantile > 1.0) {
      throw new IllegalArgumentException(quantile + " is not in [0..1]");
    }

    if (values.length == 0) {
      return 0.0;
    }

    final double pos = quantile * (values.length + 1);

    if (pos < 1) {
      return values[0];
    }

    if (pos >= values.length) {
      return values[values.length - 1];
    }

    final double lower = values[(int) pos - 1];
    final double upper = values[(int) pos];
    return lower + (pos - floor(pos)) * (upper - lower);
  }

  @Override
  public PerformanceContext startIo(Rw rw, long offset, int length) {
    logger.debug("{} {}:{}", rw, offset, offset + length);
    return internalSubmit(offset);
  }

  private PerformanceContext internalSubmit(long offset) {
    long startTime = clock.getTick();

    offsetReservoir.update(offset);
    return () -> {
      pendingCount.decrementAndGet();
      long doneTime = clock.getTick();
      long lastDone = lastDoneTime.getAndSet(doneTime);

      if (lastDone > startTime) {
        long serviceTime = doneTime - lastDone;
        latencyReservoir.update(serviceTime);
        logger.debug("offset:{} servicesTime:{}", offset, serviceTime);
      } else {
        long elapsed = doneTime - startTime;
        latencyReservoir.update(elapsed);
        logger.debug("offset:{} elapsed:{}", offset, elapsed);
      }
    };
  }

  @Override
  public int getSize() {
    int offsets = offsetReservoir.size();
    int latencies = latencyReservoir.size();
    return Math.min(offsets, latencies);
  }

  @Override
  public void dump() {
    Long[] offsets = offsetReservoir.getSnapshot();
    Long[] latencies = latencyReservoir.getSnapshot();
    if (latencies.length == 0) {
      return;
    }
    Snapshot snapshot = new UniformSnapshot(Arrays.asList(latencies));
    int n = 10;
    logger.warn("mean {}", snapshot.getMean());
    for (int i = 0; i < n; i++) {
      double quantile = i / (double) n;
      logger.warn("{} : {} {}", quantile, internalCheck(offsets, latencies, quantile, 0.1),
          snapshot.getValue(quantile));
    }
    for (int i = 0; i < n; i++) {
      double quantile = (n - 1) / (double) n + i / (double) n / n;
      logger.warn("{} : {} {}", quantile, internalCheck(offsets, latencies, quantile, 0.1),
          snapshot.getValue(quantile));
    }
  }

}
