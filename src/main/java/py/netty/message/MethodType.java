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

package py.netty.message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.Validate;

public enum MethodType implements MethodTypeInterface {
  PING(0) {
    @Override
    public boolean hasDefineDecodeMethod() {
      return false;
    }
  },
  WRITE(1) {
    @Override
    public boolean requestCarryData() {
      return true;
    }
  },
  READ(2) {
    @Override
    public boolean responseCarryData() {
      return true;
    }

    @Override
    public boolean needReleaseMsgDataInRpc() {
      return false;
    }
  },
  COPY(3) {
    @Override
    public boolean requestCarryData() {
      return true;
    }
  },
  BACKWARDSYNCLOG(4),
  SYNCLOG(5),
  CHECK(6),
  GIVEYOULOGID(7),
  GETMEMBERSHIP(8),
  ADDORCOMMITLOGS(9),
  DISCARD(10),
  STARTONLINEMIGRATION(11),
  INVALID(-1) {
    @Override
    public boolean hasDefineMethod() {
      return false;
    }

    @Override
    public boolean hasDefineDecodeMethod() {
      return false;
    }
  },
  ;

  private static final Map<Integer, MethodType> valueToEnum = Collections
      .unmodifiableMap(valueToMap());
  private final int value;

  private MethodType(int value) {
    this.value = value;
  }

  private static Map<Integer, MethodType> valueToMap() {
    Map<Integer, MethodType> map = new HashMap<>();
    for (MethodType type : MethodType.values()) {
      Validate.isTrue(null == map.put(type.getValue(), type));
    }
    return map;
  }

  public static MethodType findByValue(Integer value) {
    return valueToEnum.get(value);
  }

  public int getValue() {
    return value;
  }

}
