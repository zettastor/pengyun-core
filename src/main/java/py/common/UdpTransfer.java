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

package py.common;

import py.common.struct.EndPoint;

public interface UdpTransfer {
  /**
   * this method send data to the endpoint by udp socket.
   */
  public void writeBytes(byte[] bytedata, EndPoint endPoint);

  public void writeBytes(byte[] bytedata, int length, EndPoint endPoint);

  /**
   * this method read data from udp socket.
   *
   * @return actual read data length
   */
  public int readBytes(byte[] bytedata);

  public int readBytes(byte[] bytedata, int length);

}
