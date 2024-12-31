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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource({"classpath:config/storage.properties"})
public class StorageConfiguration {
  @Value("${segment.size.byte:1073741824}")
  private long segmentSizeByte = 1073741824L;

  @Value("${page.size.byte:8192}")
  private long pageDataSizeByte = 8192;

  @Value("${io.timeout.ms:120000}")
  private int ioTimeoutMs;

  @Value("${fs.block.size.byte:819200}")
  private long fsBlockSizeByte = 819200L;

  @Value("${enventdata.output.rootpath:/var/testing}")
  private String outputRootpath = "/var/testing";

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  public long getSegmentSizeByte() {
    return segmentSizeByte;
  }

  public void setSegmentSizeByte(long segmentSizeByte) {
    this.segmentSizeByte = segmentSizeByte;
  }

  public long getPageSizeByte() {
    return pageDataSizeByte;
  }

  public void setPageSizeByte(long pageDataSizeByte) {
    this.pageDataSizeByte = pageDataSizeByte;
  }

  public int getIoTimeoutMs() {
    return ioTimeoutMs;
  }

  public void setIoTimeoutMs(int ioTimeoutMs) {
    this.ioTimeoutMs = ioTimeoutMs;
  }

  public String getOutputRootpath() {
    return outputRootpath;
  }

  public void setOutputRootpath(String outputRootpath) {
    this.outputRootpath = outputRootpath;
  }

  public long getFsBlockSizeByte() {
    return fsBlockSizeByte;
  }

  public void setFsBlockSizeByte(long fsBlockSizeByte) {
    this.fsBlockSizeByte = fsBlockSizeByte;
  }

  @Override
  public String toString() {
    return "StorageConfiguration [segmentSizeByte=" + segmentSizeByte + ", pageDataSizeByte="
        + pageDataSizeByte
        + ", ioTimeoutMS=" + ioTimeoutMs + ", fsBlockSizeByte=" + fsBlockSizeByte + "]";
  }

  @Bean
  public EdRootpathSingleton rootpathSingleton() {
    EdRootpathSingleton edRootpathSingleton = EdRootpathSingleton.getInstance();
    edRootpathSingleton.setRootPath(outputRootpath);
    return edRootpathSingleton;
  }

}
