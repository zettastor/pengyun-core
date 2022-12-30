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
