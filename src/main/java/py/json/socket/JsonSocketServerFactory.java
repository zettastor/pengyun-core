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
