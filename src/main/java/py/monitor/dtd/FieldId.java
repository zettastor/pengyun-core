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
