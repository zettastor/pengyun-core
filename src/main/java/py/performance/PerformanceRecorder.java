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
