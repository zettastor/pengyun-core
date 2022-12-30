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

package py.storage.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import py.storage.Storage;

public class StorageUtils {
  public static final Pattern rawPattern = Pattern.compile("raw\\d+", Pattern.CASE_INSENSITIVE);
  public static final Pattern ssdPattern = Pattern.compile("ssd\\d+", Pattern.CASE_INSENSITIVE);
  public static final Pattern pciePattern = Pattern.compile("pcie\\d+", Pattern.CASE_INSENSITIVE);

  public static boolean isSata(String devicePath) {
    String identifier = getDeviceName(devicePath);
    if (identifier != null) {
      Matcher matcher = rawPattern.matcher(identifier);
      return matcher.matches();
    }
    return false;
  }

  public static boolean isSata(Storage storage) {
    return isSata(storage.identifier());
  }

  public static boolean isSsd(String devicePath) {
    String identifier = getDeviceName(devicePath);
    if (identifier != null) {
      Matcher matcher = ssdPattern.matcher(identifier);
      return matcher.matches();
    }
    return false;
  }

  public static boolean isSsd(Storage storage) {
    return isSsd(storage.identifier());
  }

  public static boolean isPcie(String devicePath) {
    String identifier = getDeviceName(devicePath);
    if (identifier != null) {
      Matcher matcher = pciePattern.matcher(identifier);
      return matcher.matches();
    }
    return false;
  }

  public static boolean isPcie(Storage storage) {
    return isPcie(storage.identifier());
  }

  private static String getDeviceName(String identifier) {
    String deviceName = identifier;
    if (deviceName != null && deviceName.contains("/")) {
      deviceName = deviceName.substring(deviceName.lastIndexOf('/') + 1);
    }

    return deviceName;
  }
}
