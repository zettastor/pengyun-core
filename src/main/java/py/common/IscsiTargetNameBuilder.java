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
