

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
