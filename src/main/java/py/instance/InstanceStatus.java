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

public enum InstanceStatus {
  HEALTHY(1),
  SUSPEND(2),
  SICK(3),
  FAILED(4),
  DISUSED(5);

  private final int value;

  private InstanceStatus(int value) {
    this.value = value;
  }

  public static InstanceStatus findByValue(int value) {
    for (InstanceStatus status : values()) {
      if (value == status.value) {
        return status;
      }
    }

    return null;
  }

  public int getValue() {
    return value;
  }

  public int compareWith(InstanceStatus other) {
    if (this.value > other.value) {
      return 1;
    } else if (this.value < other.value) {
      return -1;
    } else {
      return 0;
    }
  }
}
