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
