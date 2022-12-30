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

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.DirectAlignedBufferAllocator;
import py.storage.StorageType;

public class MergedDeadlineTaskSelector extends DeadlineTaskSelectorImpl<Task> {
  private static final Logger logger = LoggerFactory.getLogger(MergedDeadlineTaskSelector.class);

  private static final int MAX_MEGER_COUNT = 64;
  private static final int MAX_BUFFER_POLL_SIZE = 128;
  private final StorageType storageType;
  private final BlockingQueue<ByteBuffer> mergeBufferPool = new LinkedBlockingQueue<>(
      MAX_BUFFER_POLL_SIZE);
  private final int pagePhysicalSize;
  private volatile Task lastTask;

  public MergedDeadlineTaskSelector(Comparator<Task> comparator, int pagePhysicalSize) {
    this(comparator, StorageType.SATA, pagePhysicalSize);
  }

  public MergedDeadlineTaskSelector(Comparator<Task> comparator, StorageType storageType,
      int pagePhysicalSize) {
    super(comparator);
    this.storageType = storageType;
    this.pagePhysicalSize = pagePhysicalSize;
    if (this.storageType != StorageType.PCIE && this.storageType != StorageType.SSD) {
      initPool();
    }
  }

  private void initPool() {
    for (int i = 0; i < MAX_BUFFER_POLL_SIZE / 2; i++) {
      ByteBuffer buffer = DirectAlignedBufferAllocator
          .allocateAlignedByteBuffer(pagePhysicalSize * MAX_MEGER_COUNT);
      mergeBufferPool.offer(buffer);
    }
  }

  @Override
  public Task select() {
    if (this.storageType == StorageType.PCIE || this.storageType == StorageType.SSD) {
      return super.select();
    }

    LinkedList<Task> canBeMergedTasks = new LinkedList<>();

    if (lastTask != null) {
      canBeMergedTasks.add(lastTask);
      lastTask = null;
    }

    while (true) {
      Task task = super.select();
      if (task == null) {
        break;
      }

      if (canBeMergedTasks.isEmpty()) {
        canBeMergedTasks.add(task);
      } else if (task.canMergeBefore(canBeMergedTasks.getFirst())) {
        canBeMergedTasks.addFirst(task);
      } else if (canBeMergedTasks.getLast().canMergeBefore(task)) {
        canBeMergedTasks.addLast(task);
      } else {
        lastTask = task;
        break;
      }
      if (canBeMergedTasks.size() == MAX_MEGER_COUNT) {
        break;
      }
    }

    if (canBeMergedTasks.size() > 1) {
      logger.info("merging {} {} requests begin pos:{} end pos:{} ", canBeMergedTasks.size(),
          canBeMergedTasks.get(0).type, canBeMergedTasks.get(0).pos,
          canBeMergedTasks.get(canBeMergedTasks.size() - 1).pos);
    } else if (canBeMergedTasks.size() == 1) {
      return canBeMergedTasks.get(0);
    } else {
      return null;
    }

    return Task.merge(canBeMergedTasks, mergeBufferPool, pagePhysicalSize * MAX_MEGER_COUNT);

  }

}
