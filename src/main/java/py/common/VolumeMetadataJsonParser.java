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

package py.common;

import org.apache.log4j.Logger;

public class VolumeMetadataJsonParser {
  private final Logger logger = Logger.getLogger(VolumeMetadataJsonParser.class);
  private final String volumeMetadataJson;
  private final int version;

  public VolumeMetadataJsonParser(int version, String volumeMetadataJson) {
    this.version = version;
    this.volumeMetadataJson = volumeMetadataJson;
  }

  public VolumeMetadataJsonParser(String compositedVolumeMetadataJson) {
    if (compositedVolumeMetadataJson == null) {
      version = -1;
      volumeMetadataJson = null;
    } else {
      int index = compositedVolumeMetadataJson.indexOf(':');
      if (index == -1) {
        logger.warn("can not parse the volumeMetadataJSON:" + compositedVolumeMetadataJson);
        version = -1;
        volumeMetadataJson = null;
      } else {
        version = Integer.valueOf(compositedVolumeMetadataJson.substring(0, index));
        volumeMetadataJson = compositedVolumeMetadataJson.substring(index + 1);
      }
    }
  }

  public String getCompositedVolumeMetadataJson() {
    return version + ":" + volumeMetadataJson;
  }

  public String getVolumeMetadataJson() {
    return volumeMetadataJson;
  }

  public int getVersion() {
    return version;
  }
}
