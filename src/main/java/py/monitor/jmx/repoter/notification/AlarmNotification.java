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

package py.monitor.jmx.repoter.notification;

import py.monitor.alarmbak.AlarmMessageData;

public class AlarmNotification extends javax.management.Notification {
  public static final String LIST_ATTRIBUTE_FOR_ALARM = "list all attributes for alarm";
  private static final long serialVersionUID = -4086649735309058350L;
  private final AlarmMessageData notificationData;

  public AlarmNotification(Object source, long sequenceNumber, long timeStamp,
      AlarmMessageData data) {
    super(LIST_ATTRIBUTE_FOR_ALARM, source, sequenceNumber, timeStamp);
    this.notificationData = data;
    setUserData(this.notificationData);
  }

  public AlarmNotification(Object source, long sequenceNumber, long timeStamp, String message,
      AlarmMessageData data) {
    super(LIST_ATTRIBUTE_FOR_ALARM, source, sequenceNumber, timeStamp, message);
    this.notificationData = data;
    setUserData(this.notificationData);
  }

  public AlarmNotification(Object source, long sequenceNumber, AlarmMessageData data) {
    super(LIST_ATTRIBUTE_FOR_ALARM, source, sequenceNumber);
    this.notificationData = data;
    setUserData(this.notificationData);
  }

  public AlarmNotification(Object source, long sequenceNumber, String message,
      AlarmMessageData data) {
    super(LIST_ATTRIBUTE_FOR_ALARM, source, sequenceNumber, message);
    this.notificationData = data;
    setUserData(this.notificationData);
  }

  public AlarmMessageData getNotificationData() {
    return notificationData;
  }
}
