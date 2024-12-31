
package py.license;

import org.apache.commons.codec.binary.Base64;

public class DecodeUtils {
  private static final int CACHE_SIZE = 1024;

  public static byte[] decode(String base64) throws Exception {
    return new Base64().decodeBase64(base64);
  }

  public static String encode(byte[] bytes) throws Exception {
    return Base64.encodeBase64String(bytes);
  }
}
