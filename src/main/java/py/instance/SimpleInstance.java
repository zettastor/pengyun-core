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

package py.instance;

import py.common.struct.EndPoint;

public class SimpleInstance {
  private final InstanceId instanceId;
  private final EndPoint endPoint;

  public SimpleInstance(InstanceId instanceId, EndPoint endPoint) {
    this.instanceId = instanceId;
    this.endPoint = endPoint;
  }

  public InstanceId getInstanceId() {
    return instanceId;
  }

  public EndPoint getEndPoint() {
    return endPoint;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SimpleInstance)) {
      return false;
    }

    SimpleInstance that = (SimpleInstance) o;

    return instanceId != null ? instanceId.equals(that.instanceId) : that.instanceId == null;
  }

  @Override
  public int hashCode() {
    return instanceId != null ? instanceId.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "SimpleInstance{" + "instanceId=" + instanceId + ", endPoint=" + endPoint + '}';
  }
}
