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
