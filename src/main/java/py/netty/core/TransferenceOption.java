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
