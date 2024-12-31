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
