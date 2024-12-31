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

package py.netty.core;

import io.netty.util.internal.PlatformDependent;
import java.util.concurrent.ConcurrentMap;

public class TransferenceOption<T> {
  public static final ConcurrentMap<String, Boolean> names = PlatformDependent
      .newConcurrentHashMap();
  public static final TransferenceOption<Integer> MAX_MESSAGE_LENGTH = valueOf(
      "MAX_MESSAGE_LENGTH");
  public static final TransferenceOption<Integer> MAX_BYTES_ONCE_ALLOCATE = valueOf(
      "MAX_BYTES_ONCE_ALLOCATE");

  private String name;

  protected TransferenceOption(String name) {
    if (name == null) {
      throw new NullPointerException("name");
    }

    if (names.putIfAbsent(name, Boolean.TRUE) != null) {
      throw new IllegalArgumentException(String.format("'%s' is already in use", name));
    }

    this.name = name;
  }

  private static <T> TransferenceOption<T> valueOf(String name) {
    return new TransferenceOption<T>(name);
  }

  public String name() {
    return this.name;
  }
}
