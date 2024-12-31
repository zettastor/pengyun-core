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

package py.common.struct;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import py.exception.InvalidFormatException;


public abstract class AbstractId implements Comparable<AbstractId> {

  private static final SecureRandom srand = new SecureRandom();
  protected final long id;

  public AbstractId() {
    id = srand.nextLong() & Long.MAX_VALUE;
  }

  public AbstractId(@JsonProperty("id") long id) {
    this.id = id;
  }

  public AbstractId(AbstractId copyFrom) {
    this.id = copyFrom.id;
  }

  public AbstractId(String string) throws InvalidFormatException {
    this(string, true);
  }

  public AbstractId(String string, boolean rawFormat) throws InvalidFormatException {
    String prefix = printablePrefix();
    if (!rawFormat) {
      if (string.charAt(prefix.length()) != '[' || !string.endsWith("]") || !string.startsWith(
          prefix)) {
        throw new InvalidFormatException("Id's format must be prefix[id]. string : " + string);
      }
    }

    try {
      id = Long.parseLong(
          rawFormat ? string : string.substring(prefix.length() + 1, string.length() - 1));
    } catch (NumberFormatException e) {
      throw new InvalidFormatException("string:" + string);
    }
  }

  public AbstractId(ByteBuffer buffer) throws InvalidFormatException {
    id = buffer.getLong();
  }

  public AbstractId(byte[] bytes) throws InvalidFormatException {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    buffer.order(ByteOrder.BIG_ENDIAN);
    id = buffer.getLong();
  }

  public long getId() {
    return id;
  }

  @Override
  public String toString() {
    return printablePrefix() + "[" + Long.toString(id) + "]";
  }

  public String toRawString() {
    return Long.toString(id);
  }

  public abstract String printablePrefix();

  public int sizeInByteBuffer() {
    return Long.SIZE / 8;
  }

  public void toByteBuffer(ByteBuffer byteBuffer) {
    byteBuffer.putLong(id);
  }

  public byte[] toBytes() {
    byte[] bytes = new byte[sizeInByteBuffer()];
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
    byteBuffer.order(ByteOrder.BIG_ENDIAN);
    toByteBuffer(byteBuffer);
    return bytes;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AbstractId other = (AbstractId) obj;
    if (id != other.id) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(AbstractId o) {
    if (id > o.id) {
      return 1;
    } else if (id < o.id) {
      return -1;
    }
    return 0;
  }
}
