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
