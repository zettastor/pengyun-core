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

package py.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import py.common.struct.Pair;
import py.storage.performance.PerformanceContext;
import py.storage.performance.PerformanceRecorderImpl;
import py.storage.performance.Rw;

public class PerformanceRecorderImplTest extends TestBase {
  public void testWait(int interval) {
    long start = System.nanoTime();
    long end = 0;
    do {
      end = System.nanoTime();
    } while (start + interval >= end);
    System.out.println(end - start);
  }

  @Test
  public void testCheckSequence() throws Exception {
    int ioDepth = 1024;
    List<Pair<Long, Integer>> writes = new ArrayList<>();
    PerformanceRecorderImpl recorder1 = new PerformanceRecorderImpl(10, 10);

    for (int i = 0; i < ioDepth >> 1; i++) {
      writes.add(new Pair<>((long) i * 10, 10));
    }

    int sequenceIndex = writes.size();

    for (int i = ioDepth; i > ioDepth >> 1; i--) {
      writes.add(new Pair<>((long) i * 10, 10));
    }
    int randomIndex = writes.size();

    int mid = 1000000;
    int hig = 1500000;
    for (int i = 0; i < sequenceIndex; i++) {
      PerformanceContext context = recorder1
          .startIo(Rw.Write, writes.get(i).getFirst(), writes.get(i).getSecond());
      if (i == 0 || i == 256) {
        testWait(mid);
      } else if (i > 256) {
        testWait(1000);
      } else {
        testWait(hig);
      }
      context.stop();
    }

    for (int i = sequenceIndex; i < randomIndex; i++) {
      PerformanceContext context = recorder1
          .startIo(Rw.Write, writes.get(i).getFirst(), writes.get(i).getSecond());
      Thread.sleep(2);
      context.stop();
    }

    logger.warn("sequence index:{} random index:{}", sequenceIndex, randomIndex);
    Pair<Long, Long> val = recorder1.getVal(0.5, 0.1);
    Assert.assertTrue(val.getSecond() > mid && val.getSecond() < hig);
  }

  @Test
  public void testCheckRandom() throws Exception {
    int ioDepth = 1024;
    List<PerformanceContext> contextList = new ArrayList<>();
    List<Pair<Long, Integer>> writesSeq = new ArrayList<>();
    List<Pair<Long, Integer>> writesRand = new ArrayList<>();
    PerformanceRecorderImpl recorder1 = new PerformanceRecorderImpl(30, 10);

    for (int i = 0; i < 512; i++) {
      writesSeq.add(new Pair<>((long) i * 10, 10));
    }

    for (int i = 1023; i >= 512; i--) {
      writesRand.add(new Pair<>((long) i * 10, 10));
    }

    int mid = 3;
    int hig = 5;

    writesSeq.forEach(w -> {
      PerformanceContext context = recorder1
          .startIo(Rw.Write, w.getFirst(), w.getSecond());
      contextList.add(context);
    });
    contextList.forEach(PerformanceContext::stop);
    contextList.clear();

    int randomIndex = writesRand.size();
    for (int i = 0; i < randomIndex; i++) {
      PerformanceContext context = recorder1
          .startIo(Rw.Write, writesRand.get(i).getFirst(), writesRand.get(i).getSecond());
      if (i < 256 - 64) {
        int low = 1;

        Thread.sleep(low);
      } else if (i < 256 + 64 && i >= 256 - 64) {
        Thread.sleep(mid);
      } else {
        Thread.sleep(hig);
      }
      context.stop();
    }

    int sequenceIndex = writesSeq.size();
    logger.warn("sequence index:{} random index:{}", sequenceIndex, randomIndex);
    Pair<Long, Long> val = recorder1.getVal(0.5, 0.1);
    logger.warn("val:{}", val);
    Assert.assertTrue(
        val.getFirst() > TimeUnit.MILLISECONDS.toNanos(2) && val.getFirst() < TimeUnit.MILLISECONDS
            .toNanos(4));
  }

  @Deprecated
  @Test
  public void bacis() throws Exception {
    int ioDepth = 1000;
    List<Pair<Long, Integer>> writes = new ArrayList<>();
    for (int i = 0; i < ioDepth; i++) {
      writes.add(new Pair<>((long) i * 10, 10));
    }

    PerformanceRecorderImpl recorder1 = new PerformanceRecorderImpl(10, 10);

    List<PerformanceContext> contextList = new ArrayList<>();
    logger.warn("writes {}", writes);
    writes.forEach(w -> {
      PerformanceContext context = recorder1
          .startIo(Rw.Write, w.getFirst(), w.getSecond());
      contextList.add(context);
    });
    contextList.forEach(PerformanceContext::stop);
    contextList.clear();

    Collections.shuffle(writes);
    logger.warn("writes {}", writes);
    writes.forEach(w -> {
      PerformanceContext context = recorder1
          .startIo(Rw.Write, w.getFirst(), w.getSecond());
      contextList.add(context);
    });
    contextList.forEach(PerformanceContext::stop);
    contextList.clear();

    Pair<Long, Long> val1 = recorder1.getVal(0.25, 0.1);
    Pair<Long, Long> val2 = recorder1.getVal(0.5, 0.1);
    logger.warn("val {} {}", val1, val2);

  }

}