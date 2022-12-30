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

public class ServerProcessException extends AbstractNettyException {
  private static final long serialVersionUID = -8997454085085369500L;

  public ServerProcessException(String message) {
    super(ExceptionType.SERVEREXCEPTION, message);
  }

  public ServerProcessException(Throwable e) {
    super(ExceptionType.SERVEREXCEPTION, "server process exception: " + e);
  }

  public static ServerProcessException fromBuffer(ByteBuf buffer) {
    return new ServerProcessException(AbstractNettyException.bufferToString(buffer));
  }
}
