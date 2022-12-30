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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.codec.binary.Base64;

public class Base64Utils {
  private static final int CACHE_SIZE = 1024;

  public static byte[] decode(String base64) throws Exception {
    return new Base64().decodeBase64(base64);
  }

  public static String encode(byte[] bytes) throws Exception {
    return Base64.encodeBase64String(bytes);
  }

  public static String encodeFile(String filePath) throws Exception {
    byte[] bytes = fileToByte(filePath);
    return encode(bytes);
  }

  public static void decodeToFile(String filePath, String base64) throws Exception {
    byte[] bytes = decode(base64);
    byteArrayToFile(bytes, filePath);
  }

  public static byte[] fileToByte(String filePath) throws Exception {
    byte[] data = new byte[0];
    File file = new File(filePath);
    if (file.exists()) {
      FileInputStream in = new FileInputStream(file);
      ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
      byte[] cache = new byte[CACHE_SIZE];
      int read = 0;
      while ((read = in.read(cache)) != -1) {
        out.write(cache, 0, read);
        out.flush();
      }
      out.close();
      in.close();
      data = out.toByteArray();
    }
    return data;
  }

  public static void byteArrayToFile(byte[] bytes, String filePath) throws Exception {
    InputStream in = new ByteArrayInputStream(bytes);
    File destFile = new File(filePath);

    destFile.createNewFile();
    OutputStream out = new FileOutputStream(destFile);
    byte[] cache = new byte[CACHE_SIZE];
    int readN = 0;
    while ((readN = in.read(cache)) != -1) {
      out.write(cache, 0, readN);
      out.flush();
    }
    out.close();
    in.close();
  }
}
