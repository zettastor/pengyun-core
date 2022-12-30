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

package py.netty.exception;

import io.netty.buffer.ByteBuf;

public class GenericNettyException extends AbstractNettyException {
  private static final long serialVersionUID = -188977112277767110L;

  public GenericNettyException(Throwable e) {
    super(ExceptionType.GENERICEXCEPTION, "exception: " + e);
  }

  public GenericNettyException() {
    super(ExceptionType.GENERICEXCEPTION);
  }

  public GenericNettyException(String msg) {
    super(ExceptionType.GENERICEXCEPTION, msg);
  }

  public static GenericNettyException fromBuffer(ByteBuf buffer) {
    return new GenericNettyException(AbstractNettyException.bufferToString(buffer));
  }
}
