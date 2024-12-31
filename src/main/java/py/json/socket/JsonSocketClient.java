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

package py.json.socket;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

public class JsonSocketClient implements JsonSocket {
  private String sockName;

  public String parseJsonFromSocket() throws IOException {
    final File socketFile = new File(new File(System.getProperty("java.io.tmpdir")), sockName);

    AFUNIXSocket sock = null;
    InputStream is = null;

    try {
      sock = AFUNIXSocket.newInstance();
      sock.connect(new AFUNIXSocketAddress(socketFile));

      is = sock.getInputStream();

      byte[] headBytes = new byte[HEAD_LEN];
      int headReadLen = 0;
      while (headReadLen < HEAD_LEN) {
        headReadLen += is.read(headBytes, headReadLen, HEAD_LEN - headReadLen);
      }

      int jsonLen = ByteBuffer.wrap(headBytes).getInt();
      byte[] jsonBytes = new byte[jsonLen];
      int jsonReadLen = 0;
      while (jsonReadLen < jsonLen) {
        jsonReadLen += is.read(jsonBytes, jsonReadLen, jsonLen - jsonReadLen);
      }
      return new String(jsonBytes, Charset.forName("UTF-8"));
    } catch (IOException e) {
      throw new IOException("IOException occurred when parse json from socket");
    } finally {
      if (is != null) {
        is.close();
      }
      if (sock != null) {
        sock.close();
      }
    }

  }

  public String getSockName() {
    return sockName;
  }

  public void setSockName(String sockName) {
    this.sockName = sockName;
  }
}
