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

package py.common;

import com.lmax.disruptor.WorkHandler;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.struct.PyRingBuffer;
import py.engine.TaskEngine;

public class LoggerWorkHandler implements WorkHandler<LoggerEvent> {
  private static final Logger logger = LoggerFactory.getLogger(LoggerWorkHandler.class);
  private final AtomicBoolean enableLoggerTracer;
  private Map<Long, StartTimeAndLogs> traceRecordMap;
  private TaskEngine delayEngine;
  private AtomicInteger allMarkCount;

  public LoggerWorkHandler(TaskEngine delayEngine, Map<Long, StartTimeAndLogs> traceRecordMap,
      AtomicBoolean enableLoggerTracer) {
    this.enableLoggerTracer = enableLoggerTracer;
    this.traceRecordMap = traceRecordMap;
    this.delayEngine = delayEngine;
    this.allMarkCount = new AtomicInteger(0);
  }

  @Override
  public void onEvent(LoggerEvent loggerEvent) throws Exception {
    try {
      Validate.notNull(loggerEvent);
      if (loggerEvent.isMark()) {
        if (!enableLoggerTracer.get()) {
          return;
        }
        try {
          StartTimeAndLogs startTimeAndLogs;
          if (!traceRecordMap.containsKey(loggerEvent.getTraceId())) {
            StartTimeAndLogs preValue = traceRecordMap.putIfAbsent(loggerEvent.getTraceId(),
                new StartTimeAndLogs(System.currentTimeMillis()));
            if (preValue == null) {
              CleanZombieLogTask cleanZombieLogTask = new CleanZombieLogTask(
                  LoggerTracer.CLEAN_ZOMBIE_TRACE_LOG_DELAY_TIME_MS, loggerEvent.getTraceId());
              delayEngine.drive(cleanZombieLogTask);
            }
          }
          startTimeAndLogs = traceRecordMap.get(loggerEvent.getTraceId());
          if (startTimeAndLogs == null) {
            // means object has been removed
            return;
          }
          boolean offer = startTimeAndLogs.offerLog(loggerEvent.formatLogger());
          if (!offer && !startTimeAndLogs.isFull()) {
            logger.info("please look at the way U use tracer, still has:{} request not done",
                traceRecordMap.keySet().size());
          }
        } finally {
          logger.info("nothing need to do here");
        }
      } else if (loggerEvent.isDoneTrace()) {
        try {
          int timeoutMs = loggerEvent.getTimeoutMs();
          // determine if need to print all mark points by time out(MS)
          StartTimeAndLogs startTimeAndLogs = traceRecordMap.remove(loggerEvent.getTraceId());
          if (startTimeAndLogs != null) {
            long costTime = loggerEvent.getEndTime() - startTimeAndLogs.getStartTime();
            if (costTime > timeoutMs) {
              startTimeAndLogs.printLogs();
              logger
                  .warn("ori:{} {} cost time:{}ms longer than timeout:{}", loggerEvent.getTraceId(),
                      loggerEvent.getTraceAction(), costTime, timeoutMs);
            }

            int markCount = startTimeAndLogs.size();
            // dec size of mark point count
            allMarkCount.addAndGet(-markCount);
            startTimeAndLogs.release();
          }
        } finally {
          logger.info("nothing need to do here");
        }
      } else {
        throw new IllegalArgumentException("can not show up:" + loggerEvent);
      }
    } catch (Throwable t) {
      logger.error("logger event handler caught an exception", t);
    } finally {
      loggerEvent.release();
    }
  }

  class StartTimeAndLogs {
    private final Long startTime;
    private PyRingBuffer<String> logs;

    public StartTimeAndLogs(Long startTime) {
      this.startTime = startTime;
      this.logs = new PyRingBuffer(LoggerTracer.ONE_OBJECT_MAX_MARK_NUMBER);
    }

    public boolean offerLog(String log) {
      long currentMarkCount = allMarkCount.get();
      if (++currentMarkCount > LoggerTracer.MAX_MARK_COUNT) {
        return false;
      }

      boolean isFull = logs.isFull();
      logs.offer(log);
      if (!isFull) {
        allMarkCount.incrementAndGet();
        return true;
      }
      return false;
    }

    public void printLogs() {
      try {
        logger.warn("{}", logs.buildString());
      } catch (Exception e) {
        logger.error("caught an exception while print logs:{}", logs.size(), e);
      }
    }

    public int size() {
      return logs.size();
    }

    public Long getStartTime() {
      return startTime;
    }

    public boolean isFull() {
      return logs.isFull();
    }

    private void release() {
      logs.release();
      logs = null;
    }
  }
}
