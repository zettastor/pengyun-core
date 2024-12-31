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

package py.monitor.utils;

import java.io.IOException;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientUtils {
  private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

  private static final int connectTimeout = 5000;
  private static final int requestTimeout = 35000;
  private static int socketTimeout = 5000;

  public HttpClientUtils(int socketTimeout) {
    HttpClientUtils.socketTimeout = socketTimeout;
  }

  public void doPostParam(String url, Map<String, Object> headers,
      String paramMap, StringBuilder response)
      throws IOException {
    logger.warn("post DTO request {}", paramMap);
    CloseableHttpClient client = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost(url);

    if (headers != null) {
      for (Map.Entry<String, Object> entry : headers.entrySet()) {
        httpPost.setHeader(entry.getKey(), entry.getValue().toString());
      }
    }
    httpPost.addHeader("Content-Type", "application/json");

    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectTimeout(connectTimeout)
        .setConnectionRequestTimeout(requestTimeout)
        .setSocketTimeout(socketTimeout)
        .build();
    httpPost.setConfig(requestConfig);

    ByteArrayEntity byteArrayEntity = new ByteArrayEntity(paramMap.getBytes(),
        ContentType.APPLICATION_JSON);
    httpPost.setEntity(byteArrayEntity);

    CloseableHttpResponse execute = null;
    try {
      execute = client.execute(httpPost);
      HttpEntity entity = execute.getEntity();
      String result = EntityUtils.toString(entity);
      response.append(result);
    } finally {
      if (execute != null) {
        execute.close();
      }
      if (client != null) {
        client.close();
      }
    }
  }
}
