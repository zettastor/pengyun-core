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

package py.json.socket.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import py.common.RequestIdBuilder;
import py.json.socket.JsonGenerator;
import py.json.socket.JsonSocketClient;
import py.json.socket.JsonSocketClientFactory;
import py.json.socket.JsonSocketServer;
import py.json.socket.JsonSocketServerFactory;

public class JsonSocketTest {
  List<String> ipList = new ArrayList<String>();
  private JsonSocketServerFactory serverFactory;
  private JsonSocketClientFactory clientFactory;

  @Before
  public void init() {
    JsonGenerator jsonGenerator = new JsonGenerator() {
      @Override
      public String generate() {
        ipList.add("10.0.1.16:1234");
        ipList.add("10.0.1.17:1234");
        ipList.add("10.0.1.18:1234");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
          String json = objectMapper.writeValueAsString(ipList);
          return json;
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }

        return null;
      }
    };

    long volumeId = RequestIdBuilder.get();
    serverFactory = new JsonSocketServerFactory();
    serverFactory.setJsonGenerator(jsonGenerator);
    serverFactory.setVolumeId(volumeId);

    clientFactory = new JsonSocketClientFactory();
    clientFactory.setVolumeId(volumeId);
  }

  @Test
  public void testPassingListFromServerToClient() throws Exception {
    JsonSocketServer server = serverFactory.createJsonSocketServer();
    server.start();

    TimeUnit.SECONDS.sleep(5);

    for (int i = 0; i < 2000; i++) {
      JsonSocketClient client = clientFactory.createJsonSocketClient();
      String json = client.parseJsonFromSocket();
      ObjectMapper objectMapper = new ObjectMapper();
      List<String> ipListFromSocket = objectMapper.readValue(json, List.class);
      Assert.assertTrue(ipList.equals(ipListFromSocket));
    }
  }
}
