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

package py.informationcenter;

public class ScsiClient {
  private ScsiClientInfo scsiClientInfo;

  private long driverContainerId;

  private long driverContainerIdScsi;

  private String statusDescription;

  private String scsiDriverStatus;

  private String descriptionType;

  public ScsiClient() {
  }

  public ScsiClient(ScsiClientInfo scsiClientInfo, long driverContainerId,
      long driverContainerIdScsi,
      String statusDescription, String scsiDriverStatus, String descriptionType) {
    this.scsiClientInfo = scsiClientInfo;
    this.driverContainerId = driverContainerId;
    this.driverContainerIdScsi = driverContainerIdScsi;
    this.statusDescription = statusDescription;
    this.scsiDriverStatus = scsiDriverStatus;
    this.descriptionType = descriptionType;
  }

  public ScsiClientInfo getScsiClientInfo() {
    return scsiClientInfo;
  }

  public void setScsiClientInfo(ScsiClientInfo scsiClientInfo) {
    this.scsiClientInfo = scsiClientInfo;
  }

  public long getDriverContainerId() {
    return driverContainerId;
  }

  public void setDriverContainerId(long driverContainerId) {
    this.driverContainerId = driverContainerId;
  }

  public long getDriverContainerIdScsi() {
    return driverContainerIdScsi;
  }

  public void setDriverContainerIdScsi(long driverContainerIdScsi) {
    this.driverContainerIdScsi = driverContainerIdScsi;
  }

  public String getStatusDescription() {
    return statusDescription;
  }

  public void setStatusDescription(String statusDescription) {
    this.statusDescription = statusDescription;
  }

  public String getScsiDriverStatus() {
    return scsiDriverStatus;
  }

  public void setScsiDriverStatus(String scsiDriverStatus) {
    this.scsiDriverStatus = scsiDriverStatus;
  }

  public String getDescriptionType() {
    return descriptionType;
  }

  public void setDescriptionType(String descriptionType) {
    this.descriptionType = descriptionType;
  }

  @Override
  public String toString() {
    return "ScsiClient{"
        + "scsiClientInfo=" + scsiClientInfo
        + ", driverContainerId=" + driverContainerId
        + ", driverContainerIdScsi=" + driverContainerIdScsi
        + ", statusDescription='" + statusDescription + '\''
        + ", scsiDriverStatus='" + scsiDriverStatus + '\''
        + ", descriptionType='" + descriptionType + '\''
        + '}';
  }
}
