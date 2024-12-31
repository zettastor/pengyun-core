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

package py.storage.performance;

public class IoStatElement {
  private long readSectors;
  private long writeSectors;
  private long discardSectors;
  private long readIos;
  private long readMerges;
  private long writeIos;
  private long writeMerges;
  private long discardIos;
  private long discardMerges;
  private long readTicks;
  private long writeTicks;
  private long discardTicks;
  private long iosProgress;
  private long totalTicks;
  private long requestTicks;

  public long getReadSectors() {
    return readSectors;
  }

  public void setReadSectors(long readSectors) {
    this.readSectors = readSectors;
  }

  public long getWriteSectors() {
    return writeSectors;
  }

  public void setWriteSectors(long writeSectors) {
    this.writeSectors = writeSectors;
  }

  public long getDiscardSectors() {
    return discardSectors;
  }

  public void setDiscardSectors(long discardSectors) {
    this.discardSectors = discardSectors;
  }

  public long getReadIos() {
    return readIos;
  }

  public void setReadIos(long readIos) {
    this.readIos = readIos;
  }

  public long getReadMerges() {
    return readMerges;
  }

  public void setReadMerges(long readMerges) {
    this.readMerges = readMerges;
  }

  public long getWriteIos() {
    return writeIos;
  }

  public void setWriteIos(long writeIos) {
    this.writeIos = writeIos;
  }

  public long getWriteMerges() {
    return writeMerges;
  }

  public void setWriteMerges(long writeMerges) {
    this.writeMerges = writeMerges;
  }

  public long getDiscardIos() {
    return discardIos;
  }

  public void setDiscardIos(long discardIos) {
    this.discardIos = discardIos;
  }

  public long getDiscardMerges() {
    return discardMerges;
  }

  public void setDiscardMerges(long discardMerges) {
    this.discardMerges = discardMerges;
  }

  public long getReadTicks() {
    return readTicks;
  }

  public void setReadTicks(long readTicks) {
    this.readTicks = readTicks;
  }

  public long getWriteTicks() {
    return writeTicks;
  }

  public void setWriteTicks(long writeTicks) {
    this.writeTicks = writeTicks;
  }

  public long getDiscardTicks() {
    return discardTicks;
  }

  public void setDiscardTicks(long discardTicks) {
    this.discardTicks = discardTicks;
  }

  public long getIosProgress() {
    return iosProgress;
  }

  public void setIosProgress(long iosProgress) {
    this.iosProgress = iosProgress;
  }

  public long getTotalTicks() {
    return totalTicks;
  }

  public void setTotalTicks(long totalTicks) {
    this.totalTicks = totalTicks;
  }

  public long getRequestTicks() {
    return requestTicks;
  }

  public void setRequestTicks(long requestTicks) {
    this.requestTicks = requestTicks;
  }

  @Override
  public String toString() {
    return "IOStatElement{"
        + "readSectors=" + readSectors
        + ", writeSectors=" + writeSectors
        + ", discardSectors=" + discardSectors
        + ", readIOS=" + readIos
        + ", readMerges=" + readMerges
        + ", writeIOS=" + writeIos
        + ", writeMerges=" + writeMerges
        + ", discardIOS=" + discardIos
        + ", discardMerges=" + discardMerges
        + ", readTicks=" + readTicks
        + ", writeTicks=" + writeTicks
        + ", discardTicks=" + discardTicks
        + ", iosProgress=" + iosProgress
        + ", totalTicks=" + totalTicks
        + ", requestTicks=" + requestTicks
        + '}';
  }

}
