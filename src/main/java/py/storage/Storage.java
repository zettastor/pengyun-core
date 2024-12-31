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

package py.storage;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import py.common.DirectAlignedBufferAllocator;
import py.exception.StorageException;

public abstract class Storage implements Comparable<Storage> {
  private static final AtomicLong index = new AtomicLong(0);
  protected final String identifier;
  private final long id;
  private final int hashCode;

  private final AtomicBoolean isBroken = new AtomicBoolean(false);
  private final AtomicBoolean isClosed = new AtomicBoolean(false);
  protected int ioDepth = 1;

  public Storage(String identifier) {
    this.identifier = identifier;
    this.id = index.getAndIncrement();
    if (identifier == null) {
      hashCode = 0;
    } else {
      hashCode = identifier.hashCode();
    }
  }

  public static String getDeviceName(String identifier) {
    String deviceName = identifier;
    if (deviceName != null && deviceName.contains("/")) {
      deviceName = deviceName.substring(deviceName.lastIndexOf('/') + 1);
    }
    return deviceName;
  }

  public void close() throws StorageException {
    isClosed.set(true);
  }

  public boolean isClosed() {
    return isClosed.get();
  }

  public void open() throws StorageException {
    isClosed.set(false);
  }

  public abstract void read(long pos, byte[] dstBuf, int off, int len) throws StorageException;

  public abstract void read(long pos, ByteBuffer buffer) throws StorageException;

  public abstract void write(long pos, byte[] buf, int off, int len) throws StorageException;

  public abstract void write(long pos, ByteBuffer buffer) throws StorageException;

  public abstract long size();

  public String identifier() {
    return identifier;
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Storage)) {
      return false;
    }
    Storage other = (Storage) obj;

    if (hashCode == other.hashCode()) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public int compareTo(Storage out) {
    if (out == null) {
      return 1;
    } else {
      if (this.size() > out.size()) {
        return 1;
      } else if (this.size() == out.size()) {
        return 0;
      } else {
        return -1;
      }
    }
  }

  @Override
  public String toString() {
    return "Storage [" + identifier() + "]";
  }

  public boolean isBroken() {
    return isBroken.get();
  }

  public void setBroken(boolean isBroken) {
    this.isBroken.set(isBroken);
  }

  public long getId() {
    return id;
  }

  public int getIoDepth() {
    return ioDepth;
  }

  protected long findUsableStorageSize(int sectorSize) {
    ByteBuffer buffer = DirectAlignedBufferAllocator
        .allocateAlignedByteBuffer(sectorSize, sectorSize);

    long maxSize = 1;
    boolean done = false;
    while (!done) {
      maxSize *= 2;
      try {
        buffer.clear();
        read(maxSize * sectorSize, buffer);
      } catch (Exception ioe) {
        done = true;
      }
    }

    long lo = 0;
    long hi = maxSize;

    while (lo + 1 < hi) {
      long mid = (hi + lo) / 2;
      try {
        buffer.clear();
        read(mid * sectorSize, buffer);
        lo = mid;
      } catch (Exception ex) {
        hi = mid;
      }
    }

    return (lo + 1) * sectorSize;
  }
}
