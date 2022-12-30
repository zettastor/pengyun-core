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

import java.util.List;
import py.monitor.dtd.Field;

public class PerformanceNotification extends javax.management.Notification {
  public static final String LIST_ATTRIBUTE_FOR_PERFORMANCE = "list all attributes for performance";
  
  private static final long serialVersionUID = 8995700378381417418L;
  private final List<Field> attributes;

  public PerformanceNotification(Object source, long sequenceNumber, long timeStamp, String message,
      List<Field> attributes) {
    super(LIST_ATTRIBUTE_FOR_PERFORMANCE, source, sequenceNumber, timeStamp, message);
    this.attributes = attributes;
    setUserData(this.attributes);
  }

  public PerformanceNotification(Object source, long sequenceNumber, List<Field> attributes) {
    super(LIST_ATTRIBUTE_FOR_PERFORMANCE, source, sequenceNumber);
    this.attributes = attributes;
    setUserData(this.attributes);
  }

  public PerformanceNotification(Object source, long sequenceNumber, String message,
      List<Field> attributes) {
    super(LIST_ATTRIBUTE_FOR_PERFORMANCE, source, sequenceNumber, message);
    this.attributes = attributes;
    setUserData(this.attributes);
  }

  public PerformanceNotification(Object source, long sequenceNumber, long timeStamp,
      List<Field> attributes) {
    super(LIST_ATTRIBUTE_FOR_PERFORMANCE, source, sequenceNumber, timeStamp);
    this.attributes = attributes;
    setUserData(this.attributes);
  }

  public List<Field> getAttributes() {
    return attributes;
  }

}
