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

package py.io.sequential;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.io.sequential.IoSequentialTypeHolder.IoSequentialType;

public class RandomSequentialIdentifierV2Impl implements RandomSequentialIdentifierV2 {
  private static final Logger logger = LoggerFactory
      .getLogger(RandomSequentialIdentifierV2Impl.class);
  private final String prefix;
  IoState currentState;
  private int sequentialLimiting;
  private long lastOffset;

  public RandomSequentialIdentifierV2Impl(int sequentialLimiting, String prefix) {
    this.sequentialLimiting = sequentialLimiting;
    this.currentState = IoState.IO_RANDOM;
    this.prefix = prefix;
    logger.warn("sequentialLimiting:{} prefix:{}", this.sequentialLimiting, this.prefix);
  }

  private boolean matchTowIoContextOffset(IoSequentialTypeHolder previous,
      IoSequentialTypeHolder next) {
    if (previous.getOffset() + previous.getLength() == next.getOffset()) {
      return true;
    } else {
      return false;
    }
  }

  private void setTypeToSubIoContextList(int head, int tail,
      List<? extends IoSequentialTypeHolder> ioSequentialTypeHolders,
      IoSequentialType ioSequentialType) {
    for (int i = head; i <= tail; i++) {
      IoSequentialTypeHolder ioHolder = ioSequentialTypeHolders.get(i);
      ioHolder.setIoSequentialType(ioSequentialType);
    }
  }

  @Override
  public void judgeIoIsSequential(List<? extends IoSequentialTypeHolder> ioSequentialTypeHolders) {
    int size = ioSequentialTypeHolders.size();
    int ioContextSize = ioSequentialTypeHolders.size();
    int continuousCount = 0;
    for (int i = 0; i < size; i++) {
      IoSequentialTypeHolder ioContext = ioSequentialTypeHolders.get(i);
      logger
          .debug("io coming {}, {}, {}", lastOffset, ioContext.getLength(), ioContext.getOffset());

      if (ioContext.getOffset() == lastOffset) {
        if (continuousCount == 0) {
          ioContext.setIoSequentialType(IoSequentialType.SEQUENTIAL_TYPE);
        } else if (continuousCount > 0) {
          setTypeToSubIoContextList(i - continuousCount, i, ioSequentialTypeHolders,
              IoSequentialType.SEQUENTIAL_TYPE);
          continuousCount = 0;
        } else {
          logger.error("lastOffset:{} continuous:{} i:{} list:{}", lastOffset, continuousCount, i,
              ioSequentialTypeHolders);
        }
        lastOffset += ioContext.getLength();
        currentState = IoState.IO_SEQUENTIAL;
        continue;
      } else {
        if (i + 1 >= ioContextSize) {
          break;
        }
        if (matchTowIoContextOffset(ioContext, ioSequentialTypeHolders.get(i + 1))) {
          continuousCount++;

          if (continuousCount >= sequentialLimiting || (i + 1) == (size - 1)) {
            setTypeToSubIoContextList(i + 1 - continuousCount, i + 1, ioSequentialTypeHolders,
                IoSequentialType.SEQUENTIAL_TYPE);
            lastOffset = ioSequentialTypeHolders.get(i + 1).getOffset() + ioContext.getLength();
            i++;
            continuousCount = 0;
            logger
                .info("set sub list to sequential. index:{} continuous:{} list size:{}", i,
                    continuousCount, ioContextSize);
          }
        } else {
          setTypeToSubIoContextList(i - continuousCount, i, ioSequentialTypeHolders,
              IoSequentialType.RANDOM_TYPE);
          logger.info("set sub list to random. index:{} continuous:{} list size:{} offset:{}", i,
              continuousCount, ioContextSize,
              ioSequentialTypeHolders.get(i + 1).getOffset() - ioContext.getOffset());
          currentState = IoState.IO_RANDOM;
          continuousCount = 0;
          continue;
        }
      }
    }
  }

  enum IoState {
    IO_RANDOM, IO_SEQUENTIAL,
  }
}
