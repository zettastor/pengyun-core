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

import com.google.common.base.Objects;
import java.io.Serializable;

public class ScsiClientInfo implements Serializable {
  private static final long serialVersionUID = 1L;
  private String ipName;
  private long volumeId;
  private int snapshotId;

  public ScsiClientInfo() {
  }

  public ScsiClientInfo(String ipName, long volumeId, int snapshotId) {
    this.ipName = ipName;
    this.volumeId = volumeId;
    this.snapshotId = snapshotId;
  }

  public String getIpName() {
    return ipName;
  }

  public void setIpName(String ipName) {
    this.ipName = ipName;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ScsiClientInfo)) {
      return false;
    }
    ScsiClientInfo that = (ScsiClientInfo) o;
    return volumeId == that.volumeId
        && snapshotId == that.snapshotId
        && Objects.equal(ipName, that.ipName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(ipName, volumeId, snapshotId);
  }

  @Override
  public String toString() {
    return "ScsiClientInfo{"
        + "ipName='" + ipName + '\''
        + ", volumeId=" + volumeId
        + ", snapshotId=" + snapshotId
        + '}';
  }
}
