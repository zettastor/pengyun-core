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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum IoLimitationStatus {
  FREE(1),
  APPLING(2),
  APPLIED(3),
  CANCELING(4),

  DELETING(5),
  AVAILABLE(6);

  private static final Logger logger = LoggerFactory.getLogger(IoLimitationStatus.class);
  private int value;

  private IoLimitationStatus(int value) {
    this.setValue(value);
  }

  public static IoLimitationStatus findByName(String name) {
    switch (name) {
      case "FREE":
        return FREE;
      case "APPLING":
        return APPLING;
      case "APPLIED":
        return APPLIED;
      case "CANCELING":
        return CANCELING;
      case "DELETING":
        return DELETING;
      case "AVAILABLE":
        return AVAILABLE;
      default:
        logger.error("can not find IOLimitationStatus by name:{}", name);
        return null;
    }
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }
}
