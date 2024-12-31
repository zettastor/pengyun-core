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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.exception.IllegalIndexException;

public enum PortType {
  CONTROL(0),

  HEARTBEAT(1),

  IO(2),

  MONITOR(3),

  RECONCILER(4);

  private static final Logger logger = LoggerFactory.getLogger(PortType.class);

  private final int value;

  private PortType(int value) {
    this.value = value;
  }

  public static PortType get(int portTypeIndex) throws IllegalIndexException {
    for (PortType portType : values()) {
      if (portTypeIndex == portType.getValue()) {
        return portType;
      }
    }

    logger.error("The port type index : {} is out of bound");
    throw new IllegalIndexException();
  }

  public int getValue() {
    return value;
  }
}
