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
