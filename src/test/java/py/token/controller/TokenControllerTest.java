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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import py.periodic.UnableToStartException;
import py.test.TestBase;

public class TokenControllerTest extends TestBase {
  public TokenControllerTest() throws Exception {
    super.init();
  }

  @Before
  public void beforMethod() {
    try {
      TokenControllerCenter.getInstance().start();
    } catch (UnableToStartException e) {
      e.printStackTrace();
    }
  }

  @After
  public void afterMethod() {
    TokenControllerCenter.getInstance().stop();
  }

  public void testOneSecond() throws Exception {
    int times = 0;

    int bucketCapacity = 300;
    TokenController controller = TokenControllerUtils.generateAndRegister(bucketCapacity);
    long startTime = System.currentTimeMillis();
    logger.info("start");
    while (true) {
      if (times % bucketCapacity == 0 && times > 0) {
        logger.info("times: {}", times);
      }

      if (controller.acquireToken(300, TimeUnit.MILLISECONDS)) {
        times++;
      } else {
        break;
      }
    }

    logger.info("times: {}, cost time: {}", times, System.currentTimeMillis() - startTime);
   
    assertTrue(times == bucketCapacity || times == 2 * bucketCapacity);

    bucketCapacity = 900;
    times = 0;
    controller.updateToken(bucketCapacity);
    Thread.sleep(1100);

    startTime = System.currentTimeMillis();
    logger.info("start");
    while (true) {
      if (times % bucketCapacity == 0 && times > 0) {
        logger.info("times: {}", times);
      }

      if (controller.acquireToken(300, TimeUnit.MILLISECONDS)) {
        times++;
      } else {
        break;
      }
    }

    logger.info("times: {}, cost time: {}", times, System.currentTimeMillis() - startTime);
    assertEquals(times, bucketCapacity);
  }

  public void testMultiSecond() throws Exception {
    int times = 0;
    int multi = 6;

    int bucketCapacity = 300;
    long startTime = System.currentTimeMillis();
    TokenController controller = TokenControllerUtils.generateAndRegister(bucketCapacity);
    logger.info("start");
    while (true) {
      if (times % bucketCapacity == 0 && times > 0) {
        logger.info("times: {}", times);
      }

      controller.acquireToken();
      if (System.currentTimeMillis() - startTime < multi * 1000) {
        times++;
      } else {
        break;
      }
    }

    logger.info("times: {}, cost time: {}", times, System.currentTimeMillis() - startTime);
    assertTrue(times >= (multi - 1) * bucketCapacity);
    assertTrue(times <= (multi + 1) * bucketCapacity);
  }

}
