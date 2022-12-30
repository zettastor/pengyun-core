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
