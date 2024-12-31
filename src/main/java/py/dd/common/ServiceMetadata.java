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

package py.dd.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileOutputStream;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceMetadata {
  private static final Logger logger = LoggerFactory.getLogger(ServiceMetadata.class);

  private String serviceName;

  private ServiceStatus serviceStatus;

  @JsonIgnore
  private int pid;

  @JsonIgnore
  private int pmpid;

  private String version;
  private String errorCause;

  private String timestamp;

  public static ServiceMetadata buildFromFile(Path filePath) {
    if (!filePath.toFile().exists()) {
      logger.warn("Unable to build service metadata from file {}, due to no such file",
          filePath.toString());
      return null;
    }

    ObjectMapper objectMapper = new ObjectMapper();
    try {
      ServiceMetadata service = objectMapper.readValue(filePath.toFile(), ServiceMetadata.class);
      return service;
    } catch (Exception e) {
      logger
          .error("Catch an exception when build service metadata from file {}", filePath.toString(),
              e);
      return null;
    }
  }

  public String getErrorCause() {
    return errorCause;
  }

  public void setErrorCause(String errorCause) {
    this.errorCause = errorCause;
  }

  public boolean saveToFie(Path filePath) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      String serviceMetadataString = objectMapper.writeValueAsString(this);
      FileOutputStream fos = new FileOutputStream(filePath.toFile());
      fos.write(serviceMetadataString.getBytes());
      fos.flush();
      fos.getFD().sync();
      return true;
    } catch (Exception e) {
      logger.error("Catch an exception when save service {} to file {}", this, filePath.toString(),
          e);
      return false;
    }
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public ServiceStatus getServiceStatus() {
    return serviceStatus;
  }

  public void setServiceStatus(ServiceStatus serviceStatus) {
    this.serviceStatus = serviceStatus;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public int getPid() {
    return pid;
  }

  public void setPid(int pid) {
    this.pid = pid;
  }

  public int getPmpid() {
    return pmpid;
  }

  public void setPmpid(int pmpid) {
    this.pmpid = pmpid;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    ServiceMetadata another = (ServiceMetadata) obj;
    return (another.getServiceName().equals(serviceName)) && (another.getServiceStatus()
        == serviceStatus)
        && (another.getVersion().equals(version));
  }

  @Override
  public String toString() {
    return "ServiceMetadata [serviceName=" + serviceName + ", serviceStatus=" + serviceStatus
        + ", version="
        + version + "]";
  }

}
