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

import org.apache.commons.lang3.Validate;

public class IscsiTargetNameBuilder {
  private static final String TARGET_PREFIX = "Zettastor.IQN";
  private static final String SECOND_IQN_NAME_SPLIT_SIGN = "_";
  private static final String SECOND_IQN_NAME_POSTFIX = SECOND_IQN_NAME_SPLIT_SIGN + "1";

  public static String build(long volumeId, int snapshotId) {
    return String.format("%s:%s-%s", TARGET_PREFIX, volumeId, snapshotId);
  }

  public static String buildSecondIqnName(long volumeId, int snapshotId) {
    return build(volumeId, snapshotId) + SECOND_IQN_NAME_POSTFIX;
  }

  public static String buildSecondIqnName(String firstIqnName) {
    return firstIqnName + SECOND_IQN_NAME_POSTFIX;
  }

  public static long parseVolumeId(String targetName) {
    String originVolumeName = targetName.split(":")[1];
    Validate.notEmpty(originVolumeName, "can not be null %s", targetName);
    String returnString = null;
    if (originVolumeName.contains(SECOND_IQN_NAME_POSTFIX)) {
      returnString = originVolumeName.split(SECOND_IQN_NAME_SPLIT_SIGN)[0];
      Validate.notEmpty(returnString, "can not be null %s", originVolumeName);
    } else {
      returnString = originVolumeName;
    }
    return Long.parseLong(returnString);
  }
}
