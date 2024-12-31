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

package py.io.qos;

public class RebalanceRelativeTime {
  private long waitTime;

  public long getWaitTime() {
    return waitTime;
  }

  public void setWaitTime(long waitTime) {
    this.waitTime = waitTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RebalanceRelativeTime that = (RebalanceRelativeTime) o;

    return waitTime == that.waitTime;
  }

  @Override
  public int hashCode() {
    return (int) (waitTime ^ (waitTime >>> 32));
  }

  @Override
  public String toString() {
    return "RebalanceRelativeTime{"
        + "waitTime=" + waitTime
        + '}';
  }
}
