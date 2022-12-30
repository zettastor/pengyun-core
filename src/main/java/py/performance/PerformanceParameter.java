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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class PerformanceParameter {
  private long writeCounter;
  private long readCounter;

  private long readDataSizeBytes;
  private long writeDataSizeBytes;

  private long writeLatencyNs;
  private long readLatencyNs;

  private long recordTimeIntervalMs;

  public static PerformanceParameter fromJson(String json)
      throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(json, PerformanceParameter.class);
  }

  @Deprecated
  public static PerformanceParameter fromFile(String path)
      throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(new File(path), PerformanceParameter.class);
  }

  public long getWriteCounter() {
    return writeCounter;
  }

  public void setWriteCounter(long writeCounter) {
    this.writeCounter = writeCounter;
  }

  public long getReadCounter() {
    return readCounter;
  }

  public void setReadCounter(long readCounter) {
    this.readCounter = readCounter;
  }

  public long getReadDataSizeBytes() {
    return readDataSizeBytes;
  }

  public void setReadDataSizeBytes(long readDataSizeBytes) {
    this.readDataSizeBytes = readDataSizeBytes;
  }

  public long getWriteDataSizeBytes() {
    return writeDataSizeBytes;
  }

  public void setWriteDataSizeBytes(long writeDataSizeBytes) {
    this.writeDataSizeBytes = writeDataSizeBytes;
  }

  public long getWriteLatencyNs() {
    return writeLatencyNs;
  }

  public void setWriteLatencyNs(long writeLatencyNs) {
    this.writeLatencyNs = writeLatencyNs;
  }

  public long getReadLatencyNs() {
    return readLatencyNs;
  }

  public void setReadLatencyNs(long readLatencyNs) {
    this.readLatencyNs = readLatencyNs;
  }

  public long getRecordTimeIntervalMs() {
    return recordTimeIntervalMs;
  }

  public void setRecordTimeIntervalMs(long recordTimeIntervalMs) {
    this.recordTimeIntervalMs = recordTimeIntervalMs;
  }

  public String toJsonString() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(this);
  }

}
