
package py.json.socket;

public class JsonSocketServerFactory extends JsonSocketFactory {
  private long volumeId;

  private JsonGenerator jsonGenerator;

  public JsonSocketServer createJsonSocketServer() {
    String sockName = genSockNameFromVolumeId(volumeId);

    JsonSocketServer jsonSocketServer = new JsonSocketServer();
    jsonSocketServer.setSockName(sockName);
    jsonSocketServer.setJsonGenerator(jsonGenerator);

    return jsonSocketServer;
  }

  public long getVolumeId() {
    return volumeId;
  }

  public void setVolumeId(long volumeId) {
    this.volumeId = volumeId;
  }

  public JsonGenerator getJsonGenerator() {
    return jsonGenerator;
  }

  public void setJsonGenerator(JsonGenerator jsonGenerator) {
    this.jsonGenerator = jsonGenerator;
  }
}
