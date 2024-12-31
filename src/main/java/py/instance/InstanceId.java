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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.ByteBuffer;
import py.common.struct.AbstractId;
import py.exception.InvalidFormatException;

public class InstanceId extends AbstractId {
  public InstanceId() {
    super();
  }

  public InstanceId(@JsonProperty("id") long id) {
    super(id);
  }

  public InstanceId(InstanceId copyFrom) {
    super(copyFrom);
  }

  public InstanceId(String str) throws InvalidFormatException {
    super(str);
  }

  public InstanceId(byte[] bytes) throws InvalidFormatException {
    super(bytes);
  }

  public InstanceId(ByteBuffer buffer) throws InvalidFormatException {
    super(buffer);
  }

  @Override
  public String printablePrefix() {
    return "ii";
  }
}
