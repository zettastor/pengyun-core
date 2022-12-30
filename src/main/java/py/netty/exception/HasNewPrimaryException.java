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

import static py.netty.exception.ExceptionType.HAS_NEW_PRIMARY;

import io.netty.buffer.ByteBuf;

public class HasNewPrimaryException extends AbstractNettyException {
  public HasNewPrimaryException(String msg) {
    super(HAS_NEW_PRIMARY, msg);
  }

  public HasNewPrimaryException() {
    super(HAS_NEW_PRIMARY);
  }

  public static AbstractNettyException fromBuffer(ByteBuf byteBuf) {
    return new HasNewPrimaryException();
  }
}
