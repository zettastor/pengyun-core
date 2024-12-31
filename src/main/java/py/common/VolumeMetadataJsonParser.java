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
