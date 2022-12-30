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

package py.storage.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.util.HashedWheelTimer;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.RequestIdBuilder;
import py.consumer.AbstractMultiThreadConsumerService;
import py.consumer.ConsumerService;
import py.datanode.exception.StorageIoException;
import py.exception.StorageException;
import py.storage.PriorityStorage;
import py.storage.Storage;
import py.storage.StorageExceptionHandlerChain;
import py.storage.StorageType;
import py.storage.async.AsyncFileStorage;
import py.storage.async.Callback;
import py.storage.async.SlowDiskCallback;
import py.storage.impl.Task.TaskType;
import py.storage.impl.scheduling.TaskInfo;
import py.storage.performance.PyIoStatImpl;
import py.storage.performance.SlowDiskChecker;
import py.storage.performance.SlowDiskCheckerImpl;

public class PriorityStorageImpl extends PriorityStorage {
  private static final Logger logger = LoggerFactory.getLogger(PriorityStorageImpl.class);

  private static final double SLOW_DISK_CHECK_PERCENTILE = 0.5;

  private static final int PERFORMANCE_WINDOW_SECONDS = 10;

  private static final int DEFAULT_DEADLINE_MS_FOR_READ = 5;
  private static final int DEFAULT_DEADLINE_MS_FOR_WRITE = 500;
  private static final String CLASS_NAME = "PriorityStorageImpl";

  private final Semaphore writeToken;
  private final Semaphore readToken;

  private final AsyncStorage delegate;

  private final ConsumerService<Task> taskExecutor;

  private final StorageExceptionHandlerChain handlerChain;

  private final AtomicInteger countSubmittedLength = new AtomicInteger();

  private final ReentrantLock openCloseLock = new ReentrantLock();
  private final String deviceName;
  private final SlowDiskChecker slowDiskChecker;
  private final HashedWheelTimer hashedWheelTimer;
  private final int timeoutThreshold;
  private final Map<Long, TaskInfo> pendingTaskMap = new ConcurrentHashMap<>();
  private final int pagePhysicalSize;
  private ExecutorService callBackThreadPool = null;
  private volatile boolean isClosed = true;
  private int deadlineMsForRead = DEFAULT_DEADLINE_MS_FOR_READ;
  private int deadlineMsForWrite = DEFAULT_DEADLINE_MS_FOR_WRITE;

  public PriorityStorageImpl(AsyncStorage delegate, int highPriorityTaskLimit,
      StorageExceptionHandlerChain handlerChain, HashedWheelTimer hashedWheelTimer,
      int timeoutThreshold, int pagePhysicalSize) {
    this(delegate, highPriorityTaskLimit, handlerChain, hashedWheelTimer, timeoutThreshold,
        StorageType.SATA,
        pagePhysicalSize);
  }

  public PriorityStorageImpl(AsyncStorage delegate, int highPriorityTaskLimit,
      StorageExceptionHandlerChain handlerChain, HashedWheelTimer hashedWheelTimer,
      int timeoutThreshold, StorageType storageType, int pagePhysicalSize) {
    super(delegate.identifier());
    this.delegate = delegate;
    this.pagePhysicalSize = pagePhysicalSize;
    Validate.isTrue(highPriorityTaskLimit > delegate.getIoDepth());
    this.handlerChain = handlerChain;
    this.ioDepth = highPriorityTaskLimit;
    this.writeToken = new Semaphore(ioDepth);
    this.readToken = new Semaphore(ioDepth);

    this.deviceName = FilenameUtils.getName(delegate.identifier());
    this.hashedWheelTimer = hashedWheelTimer;
    this.timeoutThreshold = timeoutThreshold;

    this.taskExecutor = new TaskExecutor(Storage.getDeviceName(identifier) + "-driver",
        storageType);
    slowDiskChecker = new SlowDiskCheckerImpl(new PyIoStatImpl());

    try {
      open();
    } catch (StorageException e) {
      logger.error("can't open", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void open() throws StorageException {
    openCloseLock.lock();
    try {
      isClosed = false;
      delegate.open();
      taskExecutor.start();
      this.callBackThreadPool = new ThreadPoolExecutor(5, 5, 50,
          TimeUnit.SECONDS, new LinkedBlockingQueue<>(Integer.MAX_VALUE), new ThreadFactoryBuilder()
          .setNameFormat(Storage.getDeviceName(identifier) + "-callBack")
          .setUncaughtExceptionHandler(
              (t, e) -> logger.error("catch uncaught exception for thread:[{}]", t, e))
          .build());
    } finally {
      openCloseLock.unlock();
    }
  }

  @Override
  public void close() throws StorageException {
    openCloseLock.lock();
    try {
      if (isClosed) {
        return;
      }
      isClosed = true;
      logger.warn("closing storage {}", identifier, new Exception());
      taskExecutor.stop();
      if (this.callBackThreadPool != null) {
        this.callBackThreadPool.shutdownNow();
      }

      logger.warn("task executor stop");

      Iterator<TaskInfo> taskInfoIterator = pendingTaskMap.values().iterator();
      while (taskInfoIterator.hasNext()) {
        TaskInfo taskInfo = taskInfoIterator.next();
        Task task = taskInfo.getTask();
        logger.warn("task over time.  offset:{} size:{} type:{}",
            task.pos, task.buffer.remaining(), task.type);
        taskInfo.getStorageCallback().failed(new StorageException(), null);
      }
      delegate.close();
    } finally {
      openCloseLock.unlock();
    }
  }

  @Override
  public boolean isClosed() {
    return isClosed;
  }

  @Override
  public <A> void read(ByteBuffer buffer, long pos, A attachment,
      CompletionHandler<Integer, ? super A> handler,
      Priority priority) throws StorageException {
    if (isClosed) {
      handler.failed(new StorageException("storage closed"), attachment);
      return;
    }
    if (pos < 0) {
      throw new IllegalArgumentException(String.format("negative position %s %d", buffer, pos));
    }
    if (priority == Priority.HIGH) {
      acquireToken(TaskType.Read);
    }
    Task task = new Task<>(TaskType.Read, priority, pos, attachment, buffer,
        new CompletionHandler<Integer, A>() {
          @Override
          public void completed(Integer result, A attachment) {
            if (priority == Priority.HIGH) {
              releaseToken(TaskType.Read);
            }
            callBackThreadPool.execute(() -> {
              handler.completed(result, attachment);
            });
          }

          @Override
          public void failed(Throwable exc, A attachment) {
            if (priority == Priority.HIGH) {
              releaseToken(TaskType.Read);
            }

            callBackThreadPool.execute(() -> {
              handler.failed(exc, attachment);
            });
          }
        });
    if (!taskExecutor.submit(task)) {
      if (task.priority == Priority.HIGH) {
        releaseToken(TaskType.Read);
      }
      throw new StorageException("storage closed");
    }
  }

  @Override
  public <A> void write(ByteBuffer buffer, long pos, A attachment,
      CompletionHandler<Integer, ? super A> handler,
      Priority priority) throws StorageException {
    if (isClosed) {
      throw new StorageException("storage closed");
    }
    if (pos < 0 || buffer.remaining() <= 0) {
      throw new IllegalArgumentException(String.format("negative position %s %d", buffer, pos));
    }

    if (priority == Priority.HIGH) {
      acquireToken(TaskType.Write);
    }

    Task task = new Task<>(TaskType.Write, priority, pos, attachment, buffer,
        new CompletionHandler<Integer, A>() {
          @Override
          public void completed(Integer result, A attachment) {
            if (priority == Priority.HIGH) {
              releaseToken(TaskType.Write);
            }

            callBackThreadPool.execute(() -> {
              handler.completed(result, attachment);
            });
          }

          @Override
          public void failed(Throwable exc, A attachment) {
            if (priority == Priority.HIGH) {
              releaseToken(TaskType.Write);
            }

            callBackThreadPool.execute(() -> {
              handler.failed(exc, attachment);
            });
          }
        });
    if (!taskExecutor.submit(task)) {
      if (task.priority == Priority.HIGH) {
        releaseToken(TaskType.Write);
      }
      throw new StorageException("storage closed");
    }
  }

  @Override
  public long size() {
    return delegate.size();
  }

  public float getDiskUtility() {
    return ((float) countSubmittedLength.get()) / ((float) delegate.getIoDepth() + 1);
  }

  private void acquireToken(TaskType type) throws StorageException {
    if (isClosed) {
      throw new StorageException("storage is closed");
    }
    Semaphore token;
    if (type == TaskType.Read) {
      token = readToken;
    } else {
      Validate.isTrue(type == TaskType.Write);
      token = writeToken;
    }
    try {
      token.acquire();
    } catch (InterruptedException ignore) {
      logger.error("caught exception", ignore);
    }
  }

  private void releaseToken(TaskType type) {
    Semaphore token;
    if (type == TaskType.Read) {
      token = readToken;
    } else {
      Validate.isTrue(type == TaskType.Write);
      token = writeToken;
    }
    token.release();
  }

  private void processTask(Task task) {
    slowDiskChecker.incomingIo();

    Long taskKey = RequestIdBuilder.get();

    try {
      countSubmittedLength.incrementAndGet();
      CompletionHandler<Integer, Object> storageCallback = new
          CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
              pendingTaskMap.remove(taskKey);
              taskDone(task, result);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
              pendingTaskMap.remove(taskKey);
              if (exc instanceof StorageException) {
                taskFailed(task, (StorageException) exc);
              } else {
                taskFailed(task, new StorageIoException(task.pos, task.buffer.remaining()));
              }
              logger.warn("storage io error. type:{} offset:{} length:{}", task.type,
                  task.pos,
                  task.buffer.remaining(), exc);
            }
          };

      TaskInfo taskInfo = new TaskInfo(task, storageCallback);
      pendingTaskMap.put(taskKey, taskInfo);

      if (task.type == TaskType.Read) {
        delegate.read(task.buffer, task.pos, task.attachment, storageCallback);
      } else if (task.type == TaskType.Write) {
        delegate.write(task.buffer, task.pos, task.attachment, storageCallback);
      }

      taskInfo.submitDone();
    } catch (StorageException e) {
      taskFailed(task, e);
    }
  }

  private void taskDone(Task task, Integer result) {
    countSubmittedLength.decrementAndGet();
    callBackThreadPool.execute(() -> {
      task.complete(result);
    });
  }

  private void taskFailed(Task task, StorageException exc) {
    countSubmittedLength.decrementAndGet();
    callBackThreadPool.execute(() -> {
      handlerChain.processException(this, exc);
      task.failed(exc);
    });
  }

  public void checkSlowDisk(int sequential, int random, double ignore, int strategy,
      Storage storage) {
    enableSlowDisk(sequential, random, ignore, strategy,
        new SlowDiskCallback(storage, handlerChain));
  }

  public void enableSlowDisk(int sequential, int random, double ignore, int strategy,
      Callback callback) {
    if (delegate instanceof AsyncFileStorage) {
      ((AsyncFileStorage) delegate)
          .enableSlowDisk(sequential, random, ignore, strategy, callback);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void checkPendingTask() {
    long currentTime = System.currentTimeMillis();
    Iterator<TaskInfo> taskInfoIterator = pendingTaskMap.values().iterator();
    while (taskInfoIterator.hasNext()) {
      TaskInfo taskInfo = taskInfoIterator.next();
      Task task = taskInfo.getTask();
      long writeTime = currentTime - taskInfo.getSubmitTime();
      if (writeTime > timeoutThreshold) {
        logger.warn("task over time. cost:{} offset:{} size:{} type:{}", writeTime,
            task.pos, task.buffer.remaining(), task.type);
      }
    }
  }

  public void checkSlowDiskByAwait(String diskStatsFile, int awaitThreshold, String archiveName) {
    Path diskName = Paths.get(archiveName);
    String name = diskName.getFileName().toString();
    checkSlowDiskByAwait(diskStatsFile, name, awaitThreshold);
  }

  private void checkSlowDiskByAwait(String diskStatsFile, String deviceName, int awaitThreshold) {
    if (slowDiskChecker.isSlowDisk() == false) {
      boolean isSlowDisk = slowDiskChecker
          .checking(diskStatsFile, deviceName, awaitThreshold);
      if (isSlowDisk) {
        logger.warn("disk:{} be setting to slow disk.", deviceName);
        StorageException storageException = new StorageException();
        storageException.setIoException(true);
        handlerChain.processException(this, storageException);
      }
    }
  }

  public void setDeadlineMsForRead(int deadlineMsForRead) {
    this.deadlineMsForRead = deadlineMsForRead;
  }

  public void setDeadlineMsForWrite(int deadlineMsForWrite) {
    this.deadlineMsForWrite = deadlineMsForWrite;
  }

  private class TaskExecutor extends AbstractMultiThreadConsumerService<Task> {
    final DeadlineTaskSelector<Task> taskSelector;

    private TaskExecutor(String name, StorageType storageType) {
      super(1, PriorityStorageImpl.this::processTask, name);
      this.taskSelector = new MergedDeadlineTaskSelector(Task::compareToWithOffset, storageType,
          pagePhysicalSize);
    }

    @Override
    public int size() {
      return taskSelector.size();
    }

    @Override
    protected boolean enqueue(Task element) {
      int deadlineDelayMs;
      if (element.priority == Priority.HIGH) {
        deadlineDelayMs = 0;
      } else if (element.type == TaskType.Read) {
        deadlineDelayMs = deadlineMsForRead;
      } else if (element.type == TaskType.Write) {
        deadlineDelayMs = deadlineMsForWrite;
      } else {
        throw new IllegalArgumentException();
      }
      taskSelector.offer(element, deadlineDelayMs);
      return true;
    }

    @Override
    protected Task pollElement() {
      try {
        return taskSelector.select();
      } finally {
        logger.info("nothing need to do here");
      }
    }

    @Override
    protected Task pollElement(int time, TimeUnit timeUnit) throws InterruptedException {
      try {
        return taskSelector.select(time, timeUnit);
      } finally {
        logger.info("nothing need to do here");
      }
    }

    @Override
    public void start() {
      taskSelector.start();
      super.start();
    }

    @Override
    public void stop() {
      super.stop();
      try {
        taskSelector.stop();
      } catch (Throwable throwable) {
        logger.error("stop task selector hashed wheel timer failed", throwable);
      }
    }
  }
}
