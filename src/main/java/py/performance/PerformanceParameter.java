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
