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
