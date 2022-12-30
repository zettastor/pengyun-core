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

import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Header {
  public static final byte INVALID_METHOD_TYPE = -1;
  private static final Logger logger = LoggerFactory.getLogger(Header.class);
  private static final int NETWORK_MAIN_VERSION = 2;
  private static final int NETWORK_SUB_VERSION = 1;
  private byte mainMagic = NETWORK_MAIN_VERSION;
  private byte subMagic = NETWORK_SUB_VERSION;

  private byte methodType;

  private byte reserved;
  private int dataLength;
  private int metadataLength;
  private long requestId;

  private Header() {
  }

  public Header(byte methodType, int metadataLength, int dataLength, long requestId) {
    this.methodType = methodType;
    this.metadataLength = metadataLength;
    this.dataLength = dataLength;
    this.requestId = requestId;
    this.reserved = (byte) 0;
  }

  public static int headerLength() {
    return 1 + 1 + 1 + 1 + 4 + 4 + 8;
  }

  public static Header fromBuffer(ByteBuf buffer) {
    Header header = new Header();
    header.setMainMagic(buffer.readByte());
    header.setSubMagic(buffer.readByte());
    header.setMethodType(buffer.readByte());
    header.setReserved(buffer.readByte());
    header.setMetadataLength(buffer.readInt());
    header.setDataLength(buffer.readInt());
    header.setRequestId(buffer.readLong());
    if (!header.validateVersion()) {
      logger.error("header: {} not right", header);
      return null;
    } else {
      return header;
    }
  }

  public byte getMethodType() {
    return methodType;
  }

  public void setMethodType(byte methodType) {
    this.methodType = methodType;
  }

  public byte getReserved() {
    return reserved;
  }

  public void setReserved(byte reserved) {
    this.reserved = reserved;
  }

  public int getLength() {
    return dataLength + metadataLength;
  }

  public long getRequestId() {
    return requestId;
  }

  public void setRequestId(long requestId) {
    this.requestId = requestId;
  }

  public byte getMainMagic() {
    return mainMagic;
  }

  public void setMainMagic(byte mainMagic) {
    this.mainMagic = mainMagic;
  }

  public byte getSubMagic() {
    return subMagic;
  }

  public void setSubMagic(byte subMagic) {
    this.subMagic = subMagic;
  }

  public int getMetadataLength() {
    return metadataLength;
  }

  public void setMetadataLength(int metadataLength) {
    this.metadataLength = metadataLength;
  }

  public int getDataLength() {
    return dataLength;
  }

  public void setDataLength(int dataLength) {
    this.dataLength = dataLength;
  }

  public void toBuffer(ByteBuf buffer) {
    buffer.writeByte(mainMagic);
    buffer.writeByte(subMagic);
    buffer.writeByte(methodType);
    buffer.writeByte(reserved);
    buffer.writeInt(metadataLength);
    buffer.writeInt(dataLength);
    buffer.writeLong(requestId);
  }

  public void toBuffer(ByteBuffer buffer) {
    buffer.put(mainMagic);
    buffer.put(subMagic);
    buffer.put(methodType);
    buffer.put(reserved);
    buffer.putInt(metadataLength);
    buffer.putInt(dataLength);
    buffer.putLong(requestId);
  }

  public boolean validateVersion() {
    return mainMagic == NETWORK_MAIN_VERSION && subMagic == NETWORK_SUB_VERSION;
  }

  public boolean hasException() {
    return methodType == INVALID_METHOD_TYPE;
  }

  public boolean hasData() {
    return dataLength > 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Header)) {
      return false;
    }

    Header header = (Header) o;

    if (mainMagic != header.mainMagic) {
      return false;
    }
    if (subMagic != header.subMagic) {
      return false;
    }
    if (methodType != header.methodType) {
      return false;
    }
    if (dataLength != header.dataLength) {
      return false;
    }
    if (metadataLength != header.metadataLength) {
      return false;
    }
    return requestId == header.requestId;
  }

  @Override
  public int hashCode() {
    int result = (int) mainMagic;
    result = 31 * result + (int) subMagic;
    result = 31 * result + (int) methodType;
    result = 31 * result + dataLength;
    result = 31 * result + metadataLength;
    result = 31 * result + (int) (requestId ^ (requestId >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "Header [mainMagic=" + mainMagic + ", subMagic=" + subMagic + ", methodType="
        + methodType
        + ", reserved=" + reserved + ", metadataLength=" + metadataLength + ", length=" + dataLength
        + ", requestId=" + requestId + "]";
  }

}
