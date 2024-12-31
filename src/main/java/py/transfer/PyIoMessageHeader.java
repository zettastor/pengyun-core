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
