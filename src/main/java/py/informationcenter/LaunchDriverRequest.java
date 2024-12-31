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

import java.io.Serializable;

public class LaunchDriverRequest implements Serializable {
  private static final long serialVersionUID = 1L;

  public long requestId;
  public long accountId;
  public long volumeId;
  public int snapshotId;
  public int driverType;
  public int driverAmount;
  public String scsiIp;

  public LaunchDriverRequest() {
  }

  public long getRequestId() {
    return requestId;
  }

  public void setRequestId(long requestId) {
    this.requestId = requestId;
  }

  public long getAccountId() {
    return accountId;
  }

  public void setAccountId(long accountId) {
    this.accountId = accountId;
  }

  public long getVolumeId() {
    return volumeId;
  }

  public void setVolumeId(long volumeId) {
    this.volumeId = volumeId;
  }

  public int getSnapshotId() {
    return snapshotId;
  }

  public void setSnapshotId(int snapshotId) {
    this.snapshotId = snapshotId;
  }

  public int getDriverType() {
    return driverType;
  }

  public void setDriverType(int driverType) {
    this.driverType = driverType;
  }

  public int getDriverAmount() {
    return driverAmount;
  }

  public void setDriverAmount(int driverAmount) {
    this.driverAmount = driverAmount;
  }

  public String getScsiIp() {
    return scsiIp;
  }

  public void setScsiIp(String scsiIp) {
    this.scsiIp = scsiIp;
  }

  @Override
  public String toString() {
    return "LaunchDriverRequest{"
        + "requestId=" + requestId
        + ", accountId=" + accountId
        + ", volumeId=" + volumeId
        + ", snapshotId=" + snapshotId
        + ", driverType=" + driverType
        + ", driverAmount=" + driverAmount
        + ", scsiIp='" + scsiIp + '\''
        + '}';
  }
}
