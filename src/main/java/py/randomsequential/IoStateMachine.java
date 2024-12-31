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

package py.randomsequential;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoStateMachine {
  private static final Logger logger = LoggerFactory.getLogger(IoStateMachine.class);
  State state = State.IO_RANDOM;
  boolean offsetContinues;
  AtomicInteger sequentialCount = new AtomicInteger(0);
  AtomicInteger randomCount = new AtomicInteger(0);
  AtomicLong lastOffset = new AtomicLong(0);

  Sequential sequentialFms;
  Random randomFms;

  public IoStateMachine(int sequentialConditionThreshold, int randomConditionThreshold) {
    sequentialFms = new Sequential(randomConditionThreshold, sequentialConditionThreshold,
        sequentialCount, randomCount);
    randomFms = new Random(randomConditionThreshold, sequentialConditionThreshold, sequentialCount,
        randomCount);
    logger.warn("io state machine sequential threshold:{} random threshold:{}",
        sequentialConditionThreshold, randomConditionThreshold);
  }

  public State process(long offset, int length) {
    offsetContinues = lastOffset.get() == offset;
    switch (state) {
      case IO_RANDOM:
        state = randomFms.process(offsetContinues);
        break;
      case IO_SEQUENTIAL:
        state = sequentialFms.process(offsetContinues);
        break;
      default:
        break;
    }
    lastOffset.set(offset + length);
    return state;
  }

  enum State {
    IO_RANDOM, IO_SEQUENTIAL,
  }

  class Random extends Fsm {
    public Random(int randomCondition, int sequentialCondition, AtomicInteger sequentialCount,
        AtomicInteger randomCount) {
      super(randomCondition, sequentialCondition, sequentialCount, randomCount);
    }

    State process(boolean offsetContinues) {
      if (offsetContinues) {
        if (sequentialCount.incrementAndGet() > sequentialCondition) {
          randomCount.set(0);
          return State.IO_SEQUENTIAL;
        }
      } else {
        randomCount.incrementAndGet();
        sequentialCount.set(0);
      }
      return State.IO_RANDOM;
    }
  }

  class Sequential extends Fsm {
    public Sequential(int randomCondition, int sequentialConditon, AtomicInteger sequentialCount,
        AtomicInteger randomCount) {
      super(randomCondition, sequentialConditon, sequentialCount, randomCount);
    }

    State process(boolean offsetContinues) {
      if (offsetContinues) {
        if (sequentialCount.incrementAndGet() >= sequentialCondition) {
          randomCount.set(0);
        }
      } else {
        sequentialCount.set(0);
        if (randomCount.incrementAndGet() > randomCondition) {
          return State.IO_RANDOM;
        }
      }
      return State.IO_SEQUENTIAL;
    }
  }

  class Fsm {
    protected int randomCondition;
    protected int sequentialCondition;
    protected AtomicInteger sequentialCount;
    protected AtomicInteger randomCount;

    public Fsm(int randomCondition, int sequentialCondition, AtomicInteger sequentialCount,
        AtomicInteger randomCount) {
      this.randomCondition = randomCondition;
      this.sequentialCondition = sequentialCondition;
      this.sequentialCount = sequentialCount;
      this.randomCount = randomCount;
    }
  }

}
