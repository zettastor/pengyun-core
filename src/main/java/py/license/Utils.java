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

package py.license;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
  private static final Logger logger = LoggerFactory.getLogger(Utils.class);

  public static String compsiteStr(String[] str) {
    if (str == null) {
      return null;
    }
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < str.length; i++) {
      if (str[i] == null) {
        break;
      }
      sb.append(str[i]);
    }
    return sb.toString();
  }

  public static String[] splitStr(String string) {
    String[] str = new String[8];
    if (string == null) {
      return null;
    }
    int length = string.length();
    int i = 0;
    while (length > 0) {
      int startIndex = 200 * i;
      int endIndex = startIndex;
      if (length < 200) {
        endIndex = startIndex + length;
      } else {
        endIndex = startIndex + 200;
      }
      str[i] = string.substring(startIndex, endIndex);
      length -= 200;
      i++;
    }
    return str;
  }

  public static byte[] readFrom(Blob blob) throws SQLException, IOException {
    if (blob == null || blob.length() <= 0) {
      return null;
    }
    byte[] receive = null;
    try {
      receive = new byte[(int) blob.length()];
      InputStream is = blob.getBinaryStream();
      is.read(receive);
    } catch (SQLException e) {
      logger.error("caught an exception", e);
      throw e;
    } catch (IOException e) {
      logger.error("caught an exception", e);
      throw e;
    }
    return receive;
  }
}
