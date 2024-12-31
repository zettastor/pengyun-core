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

package py.performance;

import java.util.concurrent.atomic.AtomicLong;

public class PerformanceRecorder {
  private AtomicLong writeCounter;
  private AtomicLong readCounter;

  private AtomicLong readDataSizeBytes;
  private AtomicLong writeDataSizeBytes;

  private AtomicLong writeLatencyNs;
  private AtomicLong readLatencyNs;

  private AtomicLong recordTimeIntervalMs;

  public PerformanceRecorder() {
    this.writeCounter = new AtomicLong(0L);
    this.readCounter = new AtomicLong(0L);
    this.readDataSizeBytes = new AtomicLong(0L);
    this.writeDataSizeBytes = new AtomicLong(0L);
    this.writeLatencyNs = new AtomicLong(0L);
    this.readLatencyNs = new AtomicLong(0L);
    this.recordTimeIntervalMs = new AtomicLong(0L);
  }

  public long getWriteCounter() {
    return writeCounter.get();
  }

  public void setWriteCounter(long writeCounter) {
    this.writeCounter.set(writeCounter);
  }

  public long getReadCounter() {
    return readCounter.get();
  }

  public void setReadCounter(long readCounter) {
    this.readCounter.set(readCounter);
  }

  public synchronized long getReadLatencyNs() {
    return readLatencyNs.get();
  }

  public synchronized void setReadLatencyNs(long readLatencyNs) {
    this.readLatencyNs.set(readLatencyNs);
  }

  public long getWriteLatencyNs() {
    return writeLatencyNs.get();
  }

  public void setWriteLatencyNs(long writeLatencyNs) {
    this.writeLatencyNs.set(writeLatencyNs);
  }

  public long getReadDataSizeBytes() {
    return readDataSizeBytes.get();
  }

  public void setReadDataSizeBytes(long readDataSizeBytes) {
    this.readDataSizeBytes.set(readDataSizeBytes);
  }

  public long getWriteDataSizeBytes() {
    return writeDataSizeBytes.get();
  }

  public void setWriteDataSizeBytes(long writeDataSizeBytes) {
    this.writeDataSizeBytes.set(writeDataSizeBytes);
  }

  public long getRecordTimeIntervalMs() {
    return recordTimeIntervalMs.get();
  }

  public void setRecordTimeIntervalMs(long recordTimeIntervalMs) {
    this.recordTimeIntervalMs.set(recordTimeIntervalMs);
  }

  public void addWriteCounter(long writeCounter) {
    this.writeCounter.addAndGet(writeCounter);
  }

  public void addReadCounter(long readCounter) {
    this.readCounter.addAndGet(readCounter);
  }

  public void addWriteDataSizeBytes(long writeDataSizeBytes) {
    this.writeDataSizeBytes.addAndGet(writeDataSizeBytes);
  }

  public void addReadDataSizeBytes(long readDataSizeBytes) {
    this.readDataSizeBytes.addAndGet(readDataSizeBytes);
  }

  public synchronized void addReadLatencyNs(long readLatencyNs) {
    this.readLatencyNs.addAndGet(readLatencyNs);
  }

  public void addWriteLatencyNs(long writeLatencyNs) {
    this.writeLatencyNs.addAndGet(writeLatencyNs);
  }

  private void reset() {
    writeCounter.set(0L);
    readCounter.set(0L);
    writeDataSizeBytes.set(0L);
    readDataSizeBytes.set(0L);
    writeLatencyNs.set(0L);
    readLatencyNs.set(0L);
    recordTimeIntervalMs.set(0L);
  }

  public synchronized PerformanceParameter getPerformanceParameter() {
    PerformanceParameter performanceParameter = new PerformanceParameter();
    performanceParameter.setReadCounter(getReadCounter());
    performanceParameter.setWriteCounter(getWriteCounter());
    performanceParameter.setReadDataSizeBytes(getReadDataSizeBytes());
    performanceParameter.setWriteDataSizeBytes(getWriteDataSizeBytes());
    performanceParameter.setReadLatencyNs(getReadLatencyNs());
    performanceParameter.setWriteLatencyNs(getWriteLatencyNs());

    performanceParameter.setRecordTimeIntervalMs(getRecordTimeIntervalMs());

    reset();
    return performanceParameter;
  }
}
