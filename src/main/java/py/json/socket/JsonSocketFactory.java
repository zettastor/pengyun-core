
package py.json.socket;

public class JsonSocketFactory {
  private static final String SOCK_NAME_PREFIX = "json-sock";

  public static String genSockNameFromVolumeId(long volumeId) {
    return SOCK_NAME_PREFIX + volumeId;
  }
}
