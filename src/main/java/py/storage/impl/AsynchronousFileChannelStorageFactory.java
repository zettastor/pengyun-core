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

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.Constants;
import py.common.NamedThreadFactory;
import py.exception.StorageException;
import py.storage.PyOsInfo;
import py.storage.PyOsInfo.Os;
import py.storage.Storage;
import py.storage.StorageIoType;
import py.storage.async.AsyncFileStorage;

public class AsynchronousFileChannelStorageFactory extends FileStorageFactory {
  private static final Logger logger = LoggerFactory
      .getLogger(AsynchronousFileChannelStorageFactory.class);
  private static final int DEFAULT_MAX_IO_DEPTH_HDD = 64;
  private static final int DEFAULT_MAX_IO_DEPTH_SSD = 64;
  private static Map<Storage, ExecutorService> mapStorageToExecutor = new ConcurrentHashMap<>();
  private static Map<String, Storage> mapStorageIdentifierToStorage = new ConcurrentHashMap<>();
  private static AsynchronousFileChannelStorageFactory factory
      = new AsynchronousFileChannelStorageFactory();
  private int maxThreadpoolSizePerStorage = 0;
  private int maxThreadpoolSizePerSsd = 0;
  private int maxIoDepthHdd = DEFAULT_MAX_IO_DEPTH_HDD;
  private int maxIoDepthSsd = DEFAULT_MAX_IO_DEPTH_SSD;
  private StorageIoType storageIoType = null;
  private AtomicInteger index = new AtomicInteger(0);

  private AsynchronousFileChannelStorageFactory() {
    autoCheckIoModel();
  }

  public static AsynchronousFileChannelStorageFactory getInstance() {
    return factory;
  }

  public void close(Storage storage) {
    mapStorageIdentifierToStorage.remove(storage.identifier());
    ExecutorService executorService = mapStorageToExecutor.remove(storage);
    if (executorService != null) {
      boolean success = false;
      try {
        executorService.shutdown();
        success = executorService.awaitTermination(60, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        logger.error("caught an exception", e);
      }

      if (!success) {
        logger.warn("wait for the storage {} thread pool shutdown, timeout", storage);

      }
    }
  }

  public void close() {
    List<Storage> storages = new ArrayList<Storage>(mapStorageToExecutor.keySet());
    while (!storages.isEmpty()) {
      logger.warn("close storage: {}", storages);
      close(storages.remove(0));
    }
  }

  public Storage getStorage(String storageIdentifier) {
    return mapStorageIdentifierToStorage.get(storageIdentifier);
  }

  public AsynchronousFileChannelStorageFactory setMaxThreadpoolSizePerStorage(
      int maxThreadpoolSizePerStorage) {
    this.maxThreadpoolSizePerStorage = maxThreadpoolSizePerStorage;
    return this;
  }

  public AsynchronousFileChannelStorageFactory setMaxThreadpoolSizePerSsd(
      int maxThreadpoolSizePerSsd) {
    this.maxThreadpoolSizePerSsd = maxThreadpoolSizePerSsd;
    return this;
  }

  public AsynchronousFileChannelStorageFactory setMaxIoDepthHdd(int maxIoDepthHdd) {
    this.maxIoDepthHdd = maxIoDepthHdd;
    return this;
  }

  public AsynchronousFileChannelStorageFactory setMaxIoDepthSsd(int maxIoDepthSsd) {
    this.maxIoDepthSsd = maxIoDepthSsd;
    return this;
  }

  public AsynchronousFileChannelStorageFactory setStorageIoType(StorageIoType storageIoType) {
    this.storageIoType = storageIoType;
    return this;
  }

  public void autoCheckIoModel() {
    if (storageIoType == null) {
      Os os = PyOsInfo.getOs();
      logger.warn("os:name:{} version:{}", os, os.getVersion());
      if (os.equals(Os.LINUX)) {
        storageIoType = StorageIoType.LINUXAIO;
      } else {
        storageIoType = StorageIoType.SYNCAIO;
      }
    } else {
      logger.warn("storageIOType:{}", storageIoType);
      return;
    }

    if (!storageIoType.equals(StorageIoType.LINUXAIO)) {
      return;
    }

    boolean hasLib = false;

    String libPaths = System.getProperty("java.library.path");
    String[] libPath = libPaths.split(":");
    for (String path : libPath) {
      String libFile = path + "/liblinux-async-io.so";
      File file = new File(libFile);
      if (file.exists()) {
        hasLib = true;
        logger.warn("find lib:{}", libFile);
        return;
      }
    }

    if (!hasLib) {
      storageIoType = StorageIoType.SYNCAIO;
      logger.error("OS is linux, but can't find lib");
    }
  }

  @Override
  public Storage generate(String id) throws StorageException {
    if (StorageUtils.isSata(id)) {
      logger.warn("this is a sata disk: {}", id);
      return generateStorage(id, maxThreadpoolSizePerStorage * 4, maxIoDepthHdd,
          maxThreadpoolSizePerStorage);
    } else if (StorageUtils.isSsd(id)) {
      logger.warn("this is a ssd disk: {}", id);
      return generateStorage(id, maxThreadpoolSizePerSsd * 4, maxIoDepthSsd,
          maxThreadpoolSizePerSsd);
    } else if (StorageUtils.isPcie(id)) {
      logger.warn("this is a pcie disk: {}", id);
      return generateStorage(id, maxThreadpoolSizePerSsd * 4, maxIoDepthSsd,
          maxThreadpoolSizePerSsd);
    } else {
      logger.warn("this is a default sata disk: {}", id);
      return generateStorage(id, maxThreadpoolSizePerStorage * 4, maxIoDepthHdd,
          maxThreadpoolSizePerStorage);
    }
  }

  public Storage generateStorage(String identifier, int queueSize, int ioDepth, int threadPoolSize)
      throws StorageException {
    ThreadPoolExecutor threadPoolExecutor = null;
    try {
      String deviceName = identifier;
      if (deviceName != null && deviceName.contains("/")) {
        deviceName = deviceName.substring(deviceName.lastIndexOf('/') + 1);
      }

      @SuppressWarnings("serial")
      BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(queueSize) {
        @Override
        public boolean offer(Runnable runnable) {
          try {
            put(runnable);
            return true;
          } catch (InterruptedException e) {
            logger.error("caught an exception", e);
            Thread.currentThread().interrupt();
          }
          return false;
        }
      };

      threadPoolExecutor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 60L,
          TimeUnit.SECONDS, queue,
          new NamedThreadFactory(
              "Async-IO-Threadpool-" + deviceName + "-" + index.getAndIncrement()));

      threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

      logger.warn("identifier:{} storageIOType:{}", identifier, storageIoType);
      Storage storage;
      if (storageIoType.equals(StorageIoType.LINUXAIO)) {
        storage = new AsyncFileStorage(Paths.get(identifier), ioDepth, Constants.SECTOR_SIZE);
      } else {
        storage = new AsynchronousFileChannelStorage(Paths.get(identifier), queueSize,
            threadPoolSize,
            threadPoolExecutor);
      }
      mapStorageIdentifierToStorage.put(identifier, storage);
      ExecutorService previousExecutor = mapStorageToExecutor.put(storage, threadPoolExecutor);
      if (previousExecutor != null) {
        logger.warn("previousExecutor should be shutdown now, identifier:{}", identifier);
        previousExecutor.shutdown();
      }
      return storage;
    } catch (Exception e) {
      logger.error("caught an exception", e);
      if (threadPoolExecutor != null) {
        threadPoolExecutor.shutdown();
      }

      throw e;
    }
  }
}
