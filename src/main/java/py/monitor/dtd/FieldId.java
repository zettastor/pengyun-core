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

package py.monitor.dtd;

import java.io.Serializable;
import java.util.UUID;

public class FieldId implements Serializable {
  private static final long serialVersionUID = -221542078953395087L;

  private long taskId;

  private long metadataNumber;

  private UUID dataIndex;

  public FieldId(long taskId, long metadataNumber, UUID dataIndex) {
    this.taskId = taskId;
    this.metadataNumber = metadataNumber;
    this.dataIndex = dataIndex;
  }

  public long getMetadataNumber() {
    return metadataNumber;
  }

  public void setMetadataNumber(long fieldMetadataNumber) {
    this.metadataNumber = fieldMetadataNumber;
  }

  public UUID getDataIndex() {
    return dataIndex;
  }

  public void setDataIndex(UUID dataIndex) {
    this.dataIndex = dataIndex;
  }

  public long getTaskId() {
    return taskId;
  }

  public void setTaskId(long taskId) {
    this.taskId = taskId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dataIndex == null) ? 0 : dataIndex.hashCode());
    result = prime * result + (int) (metadataNumber ^ (metadataNumber >>> 32));
    result = prime * result + (int) (taskId ^ (taskId >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FieldId other = (FieldId) obj;
    if (dataIndex == null) {
      if (other.dataIndex != null) {
        return false;
      }
    } else if (!dataIndex.equals(other.dataIndex)) {
      return false;
    }
    if (metadataNumber != other.metadataNumber) {
      return false;
    }
    if (taskId != other.taskId) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return metadataNumber + ":" + dataIndex;
  }

}
