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
