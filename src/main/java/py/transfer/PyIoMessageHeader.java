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

public class PyIoMessageHeader {
  private int magic;
  private int length;
  private long requestid;
  private int channelid;

  public PyIoMessageHeader(int magic, int length, long requestid, int channelid) {
    this.magic = magic;
    this.length = length;
    this.requestid = requestid;
    this.channelid = channelid;
  }

  public PyIoMessageHeader() {
  }

  public long getRequestid() {
    return requestid;
  }

  public void setRequestid(long requestid) {
    this.requestid = requestid;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public int getMagic() {
    return magic;
  }

  public void setMagic(int magic) {
    this.magic = magic;
  }

  public int getChannelid() {
    return channelid;
  }

  public void setChannelid(int channelid) {
    this.channelid = channelid;
  }

  @Override
  public Object clone() {
    return new PyIoMessageHeader(this.magic, this.length, this.requestid, this.channelid);
  }

  @Override
  public String toString() {
    return "PYIOMessageHeader [magic=" + magic + ", length=" + length + ", requestid=" + requestid
        + ", channelid="
        + channelid + "]";
  }

}
