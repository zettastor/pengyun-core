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
