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

package com.google.protobuf;

import java.nio.ByteBuffer;

public class ByteStringHelper {
  public static ByteString wrap(byte[] array) {
    return ByteString.wrap(array);
  }

  public static ByteString wrap(ByteBuffer buffer) {
    return ByteString.wrap(buffer);
  }

  public static ByteString wrap(byte[] array, int offset, int length) {
    return ByteString.wrap(array, offset, length);
  }

}