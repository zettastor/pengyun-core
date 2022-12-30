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

public class ResponseHeader extends Header {
  private static final int NO_EXCEPTION_TYPE = -1;
  private int exceptionType;

  public ResponseHeader(byte methodType, int metadataLength, long requestId) {
    this(methodType, metadataLength, 0, requestId);
  }

  public ResponseHeader(byte methodType, int metaLength, int dataLength, long requestId) {
    super(methodType, metaLength, dataLength, requestId);
    this.exceptionType = NO_EXCEPTION_TYPE;
  }

  public int getExceptionType() {
    return exceptionType;
  }

  public void setExceptionType(int exceptionType) {
    this.exceptionType = exceptionType;
  }

}
