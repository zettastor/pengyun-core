

package py.json.socket;

public class JsonSocketClientFactory extends JsonSocketFactory {
  private long volumeId;

  public JsonSocketClient createJsonSocketClient() {
    String sockName = genSockNameFromVolumeId(volumeId);

    JsonSocketClient jsonSocketClient = new JsonSocketClient();
    jsonSocketClient.setSockName(sockName);

    return jsonSocketClient;
  }

  public long getVolumeId() {
    return volumeId;
  }

  public void setVolumeId(long volumeId) {
    this.volumeId = volumeId;
  }
}
