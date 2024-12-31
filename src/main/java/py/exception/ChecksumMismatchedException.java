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

package py.exception;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ChecksumMismatchedException extends Exception {
  private static final long serialVersionUID = 1L;
  private byte[] expectedDigest;
  private byte[] computedDigest;

  public ChecksumMismatchedException() {
    super();
  }

  public ChecksumMismatchedException(String message) {
    super(message);
    this.expectedDigest = null;
    this.computedDigest = null;
  }

  public ChecksumMismatchedException(String message, Throwable cause) {
    super(message, cause);
  }

  public ChecksumMismatchedException(Throwable cause) {
    super(cause);
  }

  public ChecksumMismatchedException(byte[] expectedDigest, byte[] computedDigest) {
    this("", expectedDigest, computedDigest);
  }

  public ChecksumMismatchedException(String message, byte[] expectedDigest, byte[] computedDigest) {
    super(message + " checksum  mismatch. expected:" + Arrays.toString(expectedDigest) + ", actual:"
        + Arrays.toString(computedDigest));
    this.expectedDigest = expectedDigest;
    this.computedDigest = computedDigest;
  }

  public ChecksumMismatchedException(String message, long expectedDigest, long computedDigest) {
    this(message, longToBytes(expectedDigest), longToBytes(computedDigest));
  }

  public static byte[] longToBytes(long value) {
    ByteBuffer buffer = ByteBuffer.allocate(8);
    buffer.putLong(value);
    return buffer.array();
  }

  public byte[] getExpectedDigest() {
    return expectedDigest;
  }

  public byte[] getComputedDigest() {
    return computedDigest;
  }
}
