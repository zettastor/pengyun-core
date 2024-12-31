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
