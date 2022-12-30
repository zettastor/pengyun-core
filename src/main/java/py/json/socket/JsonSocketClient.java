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
