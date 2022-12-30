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

package py.transfer;

import io.netty.buffer.ByteBuf;

public class PyIoMessage {
  public static final int MAGIC_REQUEST = 0x48282150;
  public static final int MAGIC_RESPONSE = 0x48282151;

  private PyIoMessageHeader header;
  private ByteBuf body;

  public PyIoMessageHeader getHeader() {
    return header;
  }

  public void setHeader(PyIoMessageHeader header) {
    this.header = header;
  }

  public ByteBuf getBody() {
    return body;
  }

  public void setBody(ByteBuf body) {
    this.body = body;
  }

  public interface PyIoMessageContent {
    public ByteBuf getData();

    public void setData(ByteBuf data);
  }

}
