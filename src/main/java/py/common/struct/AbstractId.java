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
