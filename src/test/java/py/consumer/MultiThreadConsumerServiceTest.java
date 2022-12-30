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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeoutException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.Utils;
import py.test.TestBase;

public class MultiThreadConsumerServiceTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(SingleThreadConsumerService.class);

  private static final int SAMPLE_COUNT = 1000;

  @Test
  public void basicTest() throws TimeoutException {
    Set<Integer> completeSet = Sets.newConcurrentHashSet();
    ConsumerService<Integer> consumerService = new MultiThreadConsumerServiceWithBlockingQueue<>(5,
        integer -> {
          try {
            Thread.sleep(10);
          } catch (InterruptedException ignore) {
            logger.error("caught exception", ignore);
          }
          completeSet.add(integer);
        }, new LinkedBlockingQueue<>(), "basic-test");

    consumerService.start();

    for (int i = 0; i < SAMPLE_COUNT; i++) {
      assertTrue(consumerService.submit(i));
    }

    Utils.waitUntilConditionMatches(5, () -> completeSet.size() == SAMPLE_COUNT);
  }

  @Test
  public void testStop() throws TimeoutException {
    Set<Integer> completeSet = Sets.newConcurrentHashSet();
    ConsumerService<Integer> consumerService = new MultiThreadConsumerServiceWithBlockingQueue<>(10,
        integer -> {
          try {
            Thread.sleep(10);
          } catch (InterruptedException ignore) {
            logger.error("caught exception", ignore);
          }
          completeSet.add(integer);
        }, new LinkedBlockingQueue<>(), "basic-test");

    consumerService.start();

    for (int i = 0; i < SAMPLE_COUNT; i++) {
      assertTrue(consumerService.submit(i));
    }

    consumerService.stop();

    Utils.waitUntilConditionMatches(5, () -> completeSet.size() == SAMPLE_COUNT);
  }

  @Test
  public void testOrder() throws Exception {
    List<Integer> completeList = new CopyOnWriteArrayList<>();

    ConsumerService<Integer> consumerService = new SingleThreadConsumerService<>(
        integer -> {
          try {
            Thread.sleep(10);
          } catch (InterruptedException ignore) {
            logger.error("caught exception", ignore);
          }
          completeList.add(integer);
        }, new PriorityBlockingQueue<>(), "basic-test");

    consumerService.start();

    List<Integer> values = new ArrayList<>();
    for (int i = 0; i < SAMPLE_COUNT; i++) {
      values.add(i);
    }

    Collections.shuffle(values);
    assertEquals(SAMPLE_COUNT, consumerService.submit(values));

    consumerService.stop();

    Utils.waitUntilConditionMatches(5, () -> completeList.size() == SAMPLE_COUNT);

    int inOrderCount = 0;
    for (int i = 1; i < SAMPLE_COUNT; i++) {
      if (completeList.get(i) - completeList.get(i - 1) > 0) {
        inOrderCount++;
      }
    }
    logger.info("in order count is {}", inOrderCount);
    assertTrue(inOrderCount > SAMPLE_COUNT / 2);
  }
}