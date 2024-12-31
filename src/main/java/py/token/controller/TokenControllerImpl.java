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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenControllerImpl implements TokenController {
  private static final Logger logger = LoggerFactory.getLogger(TokenControllerImpl.class);
  private final long id;
  private final Semaphore tokenBucket;
  private volatile int tokenCapacity;

  public TokenControllerImpl(long id, int bucketCapacity) {
    Validate.isTrue(bucketCapacity > 0);
    this.id = id;
    this.tokenCapacity = bucketCapacity;
    this.tokenBucket = new Semaphore(bucketCapacity);
  }

  @Override
  public void reset() {
    tokenBucket.drainPermits();
    tokenBucket.release(tokenCapacity);
  }

  public boolean acquireToken(long timeout, TimeUnit timeUnit) {
    try {
      return tokenBucket.tryAcquire(timeout, timeUnit);
    } catch (InterruptedException e) {
      logger.info("can not acquire semaphore");
      return false;
    }
  }

  @Override
  public boolean acquireToken(int tokenCount, long timeout, TimeUnit timeUnit) {
    try {
      return tokenBucket.tryAcquire(tokenCount, timeout, timeUnit);
    } catch (InterruptedException e) {
      logger.info("can not acquire semaphore");
      return false;
    }
  }

  @Override
  public boolean acquireToken(int tokenCount) {
    validate(tokenCount);
    try {
      tokenBucket.acquire(tokenCount);
      return true;
    } catch (InterruptedException e) {
      return false;
    }
  }

  @Override
  public boolean acquireToken() {
    try {
      tokenBucket.acquire();
      return true;
    } catch (InterruptedException e) {
      return false;
    }
  }

  public int tryAcquireToken(int tokenCount) {
    if (tokenBucket.tryAcquire(tokenCount)) {
      return tokenCount;
    } else {
      try {
        tokenBucket.acquire();
        return 1;
      } catch (InterruptedException e) {
        logger.info("can not acquire semaphore");
        return 0;
      }
    }
  }

  public long getId() {
    return id;
  }

  public void updateToken(int tokenCapacity) {
    this.tokenCapacity = tokenCapacity;
  }

  public void validate(int tokenCount) {
    if (tokenCount > tokenCapacity) {
      logger.error("total token: {}, expectable token count: {}", tokenCapacity, tokenCount);
      throw new RuntimeException("taken count too large");
    }
  }

  @Override
  public String toString() {
    return "TokenControllerImpl [id=" + id + ", bucket=" + tokenBucket + ", bucketCapacity="
        + tokenCapacity + "]";
  }

}
