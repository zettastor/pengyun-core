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
