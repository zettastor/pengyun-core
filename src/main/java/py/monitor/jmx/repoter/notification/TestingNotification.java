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

public class TestingNotification extends javax.management.Notification {
  public static final String ATTRIBUTE_CHANGE = "jmx.attribute.change";

  private static final long serialVersionUID = 5385460073005138578L;

  private String attributeName = null;

  private String attributeType = null;

  private Object oldValue = null;

  private Object newValue = null;

  public TestingNotification(Object source, long sequenceNumber, long timeStamp, String msg,
      String attributeName,
      String attributeType, Object oldValue, Object newValue) {
    super(TestingNotification.ATTRIBUTE_CHANGE, source, sequenceNumber, timeStamp, msg);
    this.attributeName = attributeName;
    this.attributeType = attributeType;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public String getAttributeType() {
    return attributeType;
  }

  public Object getOldValue() {
    return oldValue;
  }

  public Object getNewValue() {
    return newValue;
  }
}
