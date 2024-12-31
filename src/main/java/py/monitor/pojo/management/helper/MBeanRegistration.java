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

package py.monitor.pojo.management.helper;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.struct.EndPoint;
import py.monitor.customizable.CustomizableMBean;
import py.monitor.pojo.management.PeriodicPojo;
import py.monitor.pojo.management.exception.ManagementException;

public class MBeanRegistration {
  private static final Logger logger = LoggerFactory.getLogger(MBeanRegistration.class);
  private final Object object;
  private final ObjectName mbeanObjectName;
  private final MBeanServer mbeanserver;
  private final EndPoint endpoint;

  public MBeanRegistration(EndPoint endpoint, Object mbean) throws MalformedObjectNameException {
    this(endpoint, mbean,
        new ObjectNameBuilder().withEndpoint(endpoint).withObjectName(mbean.getClass()).build());
  }

  public MBeanRegistration(EndPoint endpoint, Object mbean, ObjectName objectName) {
    this.endpoint = endpoint;
    this.object = mbean;
    this.mbeanObjectName = objectName;
    this.mbeanserver = ManagementFactory.getPlatformMBeanServer();
  }

  public MBeanRegistration(EndPoint endpoint, Object mbean, MBeanServer mbeanserver)
      throws MalformedObjectNameException {
    this.endpoint = endpoint;
    this.object = mbean;
    this.mbeanObjectName = new ObjectNameBuilder().withEndpoint(endpoint)
        .withObjectName(mbean.getClass()).build();
    this.mbeanserver = mbeanserver;
  }

  public MBeanRegistration(EndPoint endpoint, Object mbean, ObjectName objectName,
      MBeanServer mbeanserver) {
    this.endpoint = endpoint;
    this.object = mbean;
    this.mbeanObjectName = objectName;
    this.mbeanserver = mbeanserver;
  }

  public NotificableDynamicMBean register() throws ManagementException {
    try {
      NotificableDynamicMBean dynamicMbean = null;
      if (object instanceof PeriodicPojo) {
        dynamicMbean = new IntrospectedDynamicMBean((PeriodicPojo) object);
      } else if (object instanceof CustomizableMBean) {
        dynamicMbean = (NotificableDynamicMBean) object;
      } else {
        throw new ManagementException();
      }
      dynamicMbean.setEndpoint(endpoint);
      logger.debug("Going to regist new MBean to MBeanServer. MBean name: {}", mbeanObjectName);
      mbeanserver.registerMBean(dynamicMbean, mbeanObjectName);
      return dynamicMbean;
    } catch (Exception e) {
      throw new ManagementException(e);
    }
  }

  public void unregister() throws ManagementException {
    try {
      logger.debug("Going to unregitster mbean: {}", mbeanObjectName);
      mbeanserver.unregisterMBean(mbeanObjectName);
    } catch (Exception e) {
      throw new ManagementException(e);
    }
  }

  public Object getPojo() {
    return object;
  }

  public ObjectName getMbeanObjectName() {
    return mbeanObjectName;
  }

  public MBeanServer getMbeanserver() {
    return mbeanserver;
  }

  public EndPoint getEndpoint() {
    return endpoint;
  }

}
