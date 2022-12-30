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
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonSocketServer extends Thread implements JsonSocket {
  private static final Logger logger = LoggerFactory.getLogger(JsonSocketServer.class);

  private String sockName;

  private JsonGenerator jsonGenerator;

  @Override
  public void run() {
    final File socketFile = new File(new File(System.getProperty("java.io.tmpdir")), sockName);

    if (socketFile.exists()) {
      socketFile.delete();
    }

    AFUNIXServerSocket server = null;
    try {
      server = AFUNIXServerSocket.newInstance();
      server.bind(new AFUNIXSocketAddress(socketFile));
    } catch (IOException e) {
      logger.error("Caught an exception when binding socket {}", sockName, e);
      return;
    }

    while (!Thread.interrupted()) {
      Socket sock = null;
      OutputStream os = null;

      try {
        sock = server.accept();
        String json = jsonGenerator.generate();
        if (json == null) {
          logger.warn("Failed to generate json string");
          continue;
        }
        os = sock.getOutputStream();
        os.write(jsonToBytes(json));
      } catch (Exception e) {
        logger.warn("Caught an exception when send json to client", e);
      } finally {
        boolean errorFlag = false;

        if (os != null) {
          try {
            os.close();
          } catch (IOException e) {
            logger.error("Caught an exception when close output stream", e);
            errorFlag = true;
          }
        }
        if (sock != null) {
          try {
            sock.close();
          } catch (IOException e) {
            logger.error("Caught an exception when close socket", e);
            errorFlag = true;
          }
        }

        if (errorFlag) {
          return;
        }
      }
    }

  }

  private byte[] jsonToBytes(String json) {
    byte[] bytes = new byte[HEAD_LEN + json.length()];

    byte[] headBytes = ByteBuffer.allocate(HEAD_LEN).putInt(json.length()).array();
    for (int i = 0; i < HEAD_LEN; i++) {
      bytes[i] = headBytes[i];
    }

    byte[] jsonBytes = json.getBytes();
    for (int i = HEAD_LEN; i < bytes.length; i++) {
      bytes[i] = jsonBytes[i - HEAD_LEN];
    }

    return bytes;
  }

  public String getSockName() {
    return sockName;
  }

  public void setSockName(String sockName) {
    this.sockName = sockName;
  }

  public JsonGenerator getJsonGenerator() {
    return jsonGenerator;
  }

  public void setJsonGenerator(JsonGenerator jsonGenerator) {
    this.jsonGenerator = jsonGenerator;
  }

  @Override
  public String toString() {
    return "JsonSocketServer{" + "sockName='" + sockName + '\'' + ", jsonGenerator=" + jsonGenerator
        + '}';
  }
}
